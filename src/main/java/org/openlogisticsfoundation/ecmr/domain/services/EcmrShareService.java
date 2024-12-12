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

import ecmr.seal.verify.rest.ESeal;
import ecmr.seal.verify.rest.SealVerifyResult;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.SealedDocument;
import org.openlogisticsfoundation.ecmr.domain.exceptions.*;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.mappers.GroupPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.*;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ExternalUserRegistrationCommand;
import org.openlogisticsfoundation.ecmr.domain.services.tan.MessageProviderException;
import org.openlogisticsfoundation.ecmr.domain.services.tan.PhoneMessageProvider;
import org.openlogisticsfoundation.ecmr.persistence.entities.*;
import org.openlogisticsfoundation.ecmr.persistence.repositories.*;
import org.openlogisticsfoundation.ecmr.web.mappers.EcmrWebMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

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
    private final EcmrRepository ecmrRepository;
    private final HistoryLogService historyLogService;

    @Value("${app.frontend-url}")
    private String frontendAddress;

    @Value("${app.origin.url}")
    private String originUrl;

    public void registerExternalUser(@Valid ExternalUserRegistrationCommand command)
            throws EcmrNotFoundException, ValidationException, MessageProviderException {
        if (StringUtils.isBlank(command.getPhone())) {
            // Currently only phone is supported. Sharing an ecmr via e-mail could be a security risk
            throw new ValidationException("Phone must be filled");
        }
        EcmrEntity ecmrEntity = ecmrService.getEcmrEntity(command.getEcmrId());
        if (!command.getShareToken().equals(ecmrEntity.getShareWithCarrierToken())) {
            // Currently this is only allowed for carriers
            throw new ValidationException("Only carriers can register external users");
        }
        String tan = RandomStringUtils.randomNumeric(6);
        final ExternalUserEntity externalUserEntity = createAndSaveExternalUser(command, tan);

        createAndSaveAssigment(ecmrEntity, EcmrRole.Carrier, externalUserEntity);

        String ecmrLink = this.frontendAddress + "/ecmr-tan/{ecmrId}/{tan}".replace("{ecmrId}", command.getEcmrId().toString()).replace("{tan}", tan);
        String tanMessage = "Your tan code is " + tan + " Please enter your code or click on the following link: " + ecmrLink;
        this.phoneMessageProvider.sendMessage(command.getPhone(), tanMessage);
    }

    private ExternalUserEntity createAndSaveExternalUser(final ExternalUserRegistrationCommand command, final String tan) {
        Instant tanValidUntil = Instant.now().plus(365, ChronoUnit.DAYS);
        ExternalUserEntity externalUserEntity = new ExternalUserEntity();
        externalUserEntity.setFirstName(command.getFirstName());
        externalUserEntity.setLastName(command.getLastName());
        externalUserEntity.setCompany(command.getCompany());
        externalUserEntity.setPhone(command.getPhone());
        externalUserEntity.setEmail(command.getEmail());
        externalUserEntity.setTan(tan);
        externalUserEntity.setTanValidUntil(tanValidUntil);
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
            userAssignments = this.ecmrAssignmentRepository.findByEcmr_EcmrIdAndExternalUser_TanAndRole(
                    ecmrId, internalOrExternalUser.getExternalUser().getTan(), roleToChange);
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
        if( eSealService.verify(List.of(seal)) != SealVerifyResult.VALID) {
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
        entity.setShareWithSenderToken(RandomStringUtils.randomAlphanumeric(4));
        entity.setShareWithCarrierToken(RandomStringUtils.randomAlphanumeric(4));
        entity.setShareWithConsigneeToken(RandomStringUtils.randomAlphanumeric(4));
        entity.setShareWithReaderToken(RandomStringUtils.randomAlphanumeric(4));

        // create SealedDocumentEntity for storing sealed document
        SealedDocumentEntity sealedDocumentEntity= ecmrPersistenceMapper.toEntity(sealedDocument);
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
