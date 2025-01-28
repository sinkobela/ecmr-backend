/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.areas.six.CarrierInformation;
import org.openlogisticsfoundation.ecmr.api.model.SealedDocument;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.RateLimitException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.UserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.mappers.GroupPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.ActionType;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrShareResponse;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.ShareEcmrResult;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ExternalUserRegistrationCommand;
import org.openlogisticsfoundation.ecmr.domain.services.tan.MessageProviderException;
import org.openlogisticsfoundation.ecmr.domain.services.tan.PhoneMessageProvider;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrAssignmentEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.ExternalUserEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.GroupEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.SealedDocumentEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.SealedEcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrAssignmentRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.ExternalUserRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.SealedDocumentRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.UserRepository;
import org.openlogisticsfoundation.ecmr.web.mappers.EcmrWebMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ecmr.seal.verify.rest.ESeal;
import ecmr.seal.verify.rest.SealVerifyResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EcmrShareService {
    private final EcmrService ecmrService;
    private final EcmrCreationService ecmrCreationService;
    private final EcmrPersistenceMapper ecmrPersistenceMapper;
    private final EcmrAssignmentRepository ecmrAssignmentRepository;
    private final ExternalUserRepository externalUserRepository;
    private final PhoneMessageProvider phoneMessageProvider;
    private final UserRepository userRepository;
    private final GroupPersistenceMapper groupPersistenceMapper;
    private final AuthorisationService authorisationService;
    private final GroupService groupService;
    private final SealedDocumentRepository sealedDocumentRepository;
    private final ExternalEcmrInstanceService externalEcmrInstanceService;
    private final ESealService eSealService;
    private final EcmrWebMapper ecmrWebMapper;
    private final HistoryLogService historyLogService;

    @Value("${app.frontend-url}")
    private String frontendAddress;

    @Value("${app.origin.url}")
    private String originUrl;


    public CarrierInformation getEcmrCarrierInformation(UUID ecmrId, String ecmrToken) throws EcmrNotFoundException, ValidationException {
        EcmrEntity ecmrEntity = ecmrService.getEcmrEntity(ecmrId);
        if(!ecmrToken.equals(ecmrEntity.getShareWithCarrierToken())){
            throw new ValidationException("Only carriers can register external users");
        }
        return ecmrPersistenceMapper.map(ecmrEntity.getCarrierInformation());
    }

    ///  Return the user token
    public String registerExternalUser(@Valid ExternalUserRegistrationCommand command)
            throws EcmrNotFoundException, ValidationException, MessageProviderException, RateLimitException {
        if (StringUtils.isBlank(command.getPhone())) {
            // Currently only phone is supported. Sharing an ecmr via e-mail could be a security risk
            throw new ValidationException("Phone must be filled");
        }
        EcmrEntity ecmrEntity = ecmrService.getEcmrEntity(command.getEcmrId());
        if (!command.getShareToken().equals(ecmrEntity.getShareWithCarrierToken())) {
            // Currently this is only allowed for carriers
            throw new ValidationException("Only carriers can register external users");
        }

        List<ExternalUserEntity> existingExternalUsersByPhone = this.externalUserRepository.findByPhone(command.getPhone()); //Deactivate all other tans for this phone
        for (ExternalUserEntity externalUserEntity : existingExternalUsersByPhone) {
            externalUserEntity.setActive(false);
            externalUserRepository.save(externalUserEntity);
        }

        int registrationCountInLastHour = this.ecmrAssignmentRepository.countByEcmr_EcmrIdAndExternalUser_CreationTimestampGreaterThan(command.getEcmrId(), Instant.now().minusSeconds(3600));
        if (registrationCountInLastHour > 10) {
            throw new RateLimitException("More than 10 registrations within last hour");
        }

        String userToken = RandomStringUtils.secure().nextAlphanumeric(4);
        String tan = RandomStringUtils.secure().nextNumeric(6);
        final ExternalUserEntity externalUserEntity = createAndSaveExternalUser(command, userToken, tan);

        createAndSaveAssigment(ecmrEntity, EcmrRole.Carrier, externalUserEntity);

        String ecmrLink = this.frontendAddress + "/ecmr-tan/{ecmrId}/{user-token}/{tan}"
                .replace("{ecmrId}", command.getEcmrId().toString())
                .replace("{user-token}", userToken)
                .replace("{tan}", tan);
        String tanMessage = "Your tan code is " + tan + " Please enter your code or click on the following link: " + ecmrLink;
        this.phoneMessageProvider.sendMessage(command.getPhone(), tanMessage);
        return userToken;
    }

    private ExternalUserEntity createAndSaveExternalUser(final ExternalUserRegistrationCommand command, final String userToken, final String tan) {
        Instant tanValidUntil = Instant.now().plus(365, ChronoUnit.DAYS);
        ExternalUserEntity externalUserEntity = new ExternalUserEntity();
        externalUserEntity.setFirstName(command.getFirstName());
        externalUserEntity.setLastName(command.getLastName());
        externalUserEntity.setCompany(command.getCompany());
        externalUserEntity.setPhone(command.getPhone());
        externalUserEntity.setEmail(command.getEmail());
        externalUserEntity.setUserToken(userToken);
        externalUserEntity.setTan(tan);
        externalUserEntity.setTanValidUntil(tanValidUntil);
        externalUserEntity.setCreationTimestamp(Instant.now());
        externalUserEntity.setActive(true);
        return externalUserRepository.save(externalUserEntity);
    }

    public EcmrShareResponse shareEcmr(InternalOrExternalUser internalOrExternalUser, @Valid @NotNull UUID ecmrId, @Valid @NotNull String email,
            @Valid @NotNull EcmrRole role) throws EcmrNotFoundException, NoPermissionException, ValidationException {
        if (role == EcmrRole.Reader) {
            throw new ValidationException("Ecmr can't be shared to Reader");
        }
        List<EcmrRole> rolesOfUser = authorisationService.getRolesOfUser(internalOrExternalUser, ecmrId);
        if (!rolesOfUser.contains(EcmrRole.Sender) && !rolesOfUser.contains(EcmrRole.Carrier)) {
            throw new NoPermissionException("No Permission to share as Consignee or Readonly");
        }
        this.validateShareRoles(rolesOfUser, role);
        EcmrEntity ecmr = ecmrService.getEcmrEntity(ecmrId);
        Optional<UserEntity> userEntityOpt = userRepository.findByEmailAndDeactivatedFalse(email);
        if (userEntityOpt.isPresent()) {
            UserEntity userEntity = userEntityOpt.get();
            if (userEntity.getDefaultGroup() == null) {
                return new EcmrShareResponse(ShareEcmrResult.ErrorInternalUserHasNoGroup, null);
            } else {
                if (rolesOfUser.contains(role)) {
                    this.changeRoleToReadonly(internalOrExternalUser, role, ecmrId);
                }
                List<EcmrAssignmentEntity> assignment = this.ecmrAssignmentRepository.findByEcmr_EcmrIdAndGroup_idInAndRole(
                        ecmrId, List.of(userEntity.getDefaultGroup().getId()), role);
                if (!assignment.isEmpty()) {
                    return new EcmrShareResponse(ShareEcmrResult.SharedInternal, this.groupPersistenceMapper.toGroup(userEntity.getDefaultGroup()));
                }

                createAndSaveAssigment(ecmr, role, userEntity.getDefaultGroup());

                return new EcmrShareResponse(ShareEcmrResult.SharedInternal, this.groupPersistenceMapper.toGroup(userEntity.getDefaultGroup()));
            }
        } else {
            //TODO Implement external sharing
            throw new NotImplementedException();
        }
    }

    private void createAndSaveAssigment(final EcmrEntity ecmr, final EcmrRole role, final ExternalUserEntity externalUser) {
        EcmrAssignmentEntity assignmentEntity = new EcmrAssignmentEntity();
        assignmentEntity.setEcmr(ecmr);
        assignmentEntity.setRole(role);
        assignmentEntity.setExternalUser(externalUser);
        ecmrAssignmentRepository.save(assignmentEntity);
    }

    private void createAndSaveAssigment(final EcmrEntity ecmr, final EcmrRole role, final GroupEntity group) {
        EcmrAssignmentEntity assignmentEntity = new EcmrAssignmentEntity();
        assignmentEntity.setEcmr(ecmr);
        assignmentEntity.setRole(role);
        assignmentEntity.setGroup(group);
        ecmrAssignmentRepository.save(assignmentEntity);
    }

    private void changeRoleToReadonly(InternalOrExternalUser internalOrExternalUser, EcmrRole roleToChange, UUID ecmrId) {
        List<EcmrAssignmentEntity> userAssignments;
        if (internalOrExternalUser.isInternalUser()) {
            List<Group> usersGroups = groupService.getGroupsForUser(internalOrExternalUser.getInternalUser().getId());
            List<Long> groupIds = groupService.flatMapGroupTrees(usersGroups).stream().map(Group::getId).toList();
            userAssignments = this.ecmrAssignmentRepository.findByEcmr_EcmrIdAndGroup_idInAndRole(ecmrId,
                    groupIds, roleToChange);
        } else {
            userAssignments = this.ecmrAssignmentRepository.findByExternalUser(
                    ecmrId, internalOrExternalUser.getExternalUser().getUserToken(), internalOrExternalUser.getExternalUser().getTan())
                    .stream().filter(e -> e.getRole() == roleToChange)
                    .toList();
        }
        userAssignments.forEach(userAssignment -> userAssignment.setRole(EcmrRole.Reader));
        ecmrAssignmentRepository.saveAll(userAssignments);
    }

    private void validateShareRoles(List<EcmrRole> userRoles, EcmrRole roleToShare) throws NoPermissionException {
        switch (roleToShare) {
        case Sender:
            if (!userRoles.contains(EcmrRole.Sender)) {
                throw new NoPermissionException("Only Sender can share with Sender");
            }
            break;
        case Carrier, Consignee:
            if (!userRoles.contains(EcmrRole.Carrier) && !userRoles.contains(EcmrRole.Sender)) {
                throw new NoPermissionException("Only Carrier and Sender can share with " + roleToShare.name());
            }
            break;
        case Reader:
            if (!userRoles.contains(EcmrRole.Carrier) && !userRoles.contains(EcmrRole.Sender) && !userRoles.contains(EcmrRole.Consignee)) {
                throw new NoPermissionException("Reader cant share ecmr");
            }
            break;
        }
    }

    public String getShareToken(UUID ecmrId, EcmrRole ecmrRole, InternalOrExternalUser internalOrExternalUser)
            throws EcmrNotFoundException, NoPermissionException {
        List<EcmrRole> rolesOfUser = authorisationService.getRolesOfUser(internalOrExternalUser, ecmrId);
        this.validateShareRoles(rolesOfUser, ecmrRole);
        EcmrEntity ecmrEntity = ecmrService.getEcmrEntity(ecmrId);
        return switch (ecmrRole) {
            case Sender -> ecmrEntity.getShareWithSenderToken();
            case Consignee -> ecmrEntity.getShareWithConsigneeToken();
            case Carrier -> ecmrEntity.getShareWithCarrierToken();
            case Reader -> ecmrEntity.getShareWithReaderToken();
        };
    }

    public EcmrModel importEcmr(AuthenticatedUser authenticatedUser, UUID ecmrId, String shareToken)
            throws EcmrNotFoundException, ValidationException, UserNotFoundException {
        EcmrEntity ecmrEntity = ecmrService.getEcmrEntity(ecmrId);

        if (!shareToken.equals(ecmrEntity.getShareWithReaderToken())) {
            throw new ValidationException("You need the share token to import this ecmr");
        }

        UserEntity userEntity = userRepository.findById(authenticatedUser.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException(authenticatedUser.getUser().getId()));

        if (userEntity.getDefaultGroup() == null) {
            throw new ValidationException("No Default Group");
        }

        createAndSaveAssigment(ecmrEntity, EcmrRole.Reader, userEntity.getDefaultGroup());

        return ecmrPersistenceMapper.toModel(ecmrEntity);
    }

    @Transactional
    public SealedDocument exportEcmrToExternal(UUID ecmrId, String shareToken)
            throws ValidationException {

        SealedDocumentEntity sealedDocumentEntity = sealedDocumentRepository.findByEcmrId(ecmrId).orElseThrow();

        if (!shareToken.equals(sealedDocumentEntity.getSealedEcmr().getEcmr().getShareWithReaderToken())) {
            throw new ValidationException("You need a valid share token to import this ecmr");
        }

        return ecmrPersistenceMapper.toModel(sealedDocumentEntity);
    }

    // import existing ecmr from external instance and save it initially on this instance
    @Transactional
    public void importEcmrFromExternal(String url, UUID ecmrId, String shareToken, List<Long> groupIds, AuthenticatedUser authenticatedUser)
            throws InvalidInputException, NoPermissionException {
        // 1. call export endpoint from external instance
        SealedDocument sealedDocument = externalEcmrInstanceService.importEcmr(url, ecmrId, shareToken);

        // 2. verify sealed document before calling any internal functions
        ESeal seal = new ESeal(sealedDocument.getSeal(), null);
        if (eSealService.verify(List.of(seal)) != SealVerifyResult.VALID) {
            throw new InvalidInputException("Invalid input data");
        }

        // 3. save sealedDocumentEntity
        EcmrCommand ecmrCommand = ecmrWebMapper.toCommand(sealedDocument.getSealedEcmr().getEcmr());

        if (!groupService.areAllGroupIdsPartOfUsersGroup(authenticatedUser, groupIds)) {
            throw new NoPermissionException("No permission for at least one group id");
        }
        if (!authorisationService.validateSaveCommand(ecmrCommand)) {
            throw new NoPermissionException("Save command is not valid");
        }

        // set up EcmrEntity
        EcmrEntity entity = ecmrPersistenceMapper.toEntity(ecmrCommand, EcmrType.ECMR, sealedDocument.getSealedEcmr().getEcmr().getEcmrStatus());
        entity.setEcmrId(ecmrId);
        entity.setEcmrStatus(sealedDocument.getSealedEcmr().getEcmr().getEcmrStatus());
        entity.setOriginUrl(sealedDocument.getSealedEcmr().getEcmr().getOriginUrl());

        // create shared token for received ecmr
        entity.setShareWithSenderToken(RandomStringUtils.secure().nextAlphanumeric(4));
        entity.setShareWithCarrierToken(RandomStringUtils.secure().nextAlphanumeric(4));
        entity.setShareWithConsigneeToken(RandomStringUtils.secure().nextAlphanumeric(4));
        entity.setShareWithReaderToken(RandomStringUtils.secure().nextAlphanumeric(4));

        // create SealedDocumentEntity for storing sealed document
        SealedDocumentEntity sealedDocumentEntity = ecmrPersistenceMapper.toEntity(sealedDocument);
        SealedEcmrEntity sealedEcmr = sealedDocumentEntity.getSealedEcmr();
        sealedEcmr.setEcmr(entity);
        sealedDocumentEntity.setSealedEcmr(sealedEcmr);

        sealedDocumentRepository.save(sealedDocumentEntity);

        // set up group associations
        List<GroupEntity> groupEntities = groupService.getGroupEntities(groupIds);
        for (GroupEntity groupEntity : groupEntities) {
            EcmrAssignmentEntity ecmrAssignmentEntity = new EcmrAssignmentEntity();
            ecmrAssignmentEntity.setEcmr(entity);
            ecmrAssignmentEntity.setGroup(groupEntity);
            ecmrAssignmentEntity.setRole(EcmrRole.Sender);
            ecmrAssignmentRepository.save(ecmrAssignmentEntity);
        }

        // save history log
        this.historyLogService.writeHistoryLog(entity, sealedDocument.getSealedEcmr().getEcmr().getOriginUrl(), ActionType.Creation);
    }
}
