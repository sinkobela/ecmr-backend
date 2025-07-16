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
import org.openlogisticsfoundation.ecmr.api.model.SealedDocument;
import org.openlogisticsfoundation.ecmr.api.model.areas.six.CarrierInformation;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrAlreadyExistsException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.GroupNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.RateLimitException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.SealedDocumentNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.UserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrAssignmentMapper;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.mappers.GroupPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.mappers.SealedDocumentPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.ActionType;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrAssignment;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrExportResult;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrShareResponse;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.ShareEcmrResult;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ExternalUserRegistrationCommand;
import org.openlogisticsfoundation.ecmr.domain.services.tan.MessageProviderException;
import org.openlogisticsfoundation.ecmr.domain.services.tan.PhoneMessageProvider;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrAssignmentEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.ExternalUserEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.GroupEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.SealedDocumentEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrAssignmentRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.ExternalUserRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.GroupRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.SealedDocumentRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.UserRepository;
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
    private final SealedDocumentService sealedDocumentService;
    private final HistoryLogService historyLogService;
    private final MailService mailService;
    private final GroupRepository groupRepository;
    private final SealedDocumentPersistenceMapper sealedDocumentPersistenceMapper;

    @Value("${app.frontend-url}")
    private String frontendAddress;

    @Value("${app.origin.url}")
    private String originUrl;
    private final EcmrAssignmentMapper ecmrAssignmentMapper;

    public CarrierInformation getEcmrCarrierInformation(UUID ecmrId, String ecmrToken) throws EcmrNotFoundException, ValidationException {
        EcmrEntity ecmrEntity = ecmrService.getEcmrEntity(ecmrId);
        if (!ecmrToken.equals(ecmrEntity.getShareWithCarrierToken())) {
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

        List<ExternalUserEntity> existingExternalUsersByPhone = this.externalUserRepository.findByPhone(
                command.getPhone()); //Deactivate all other tans for this phone
        for (ExternalUserEntity externalUserEntity : existingExternalUsersByPhone) {
            externalUserEntity.setActive(false);
            externalUserRepository.save(externalUserEntity);
        }

        int registrationCountInLastHour = this.ecmrAssignmentRepository.countByEcmr_EcmrIdAndExternalUser_CreationTimestampGreaterThan(
                command.getEcmrId(), Instant.now().minusSeconds(3600));
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
        if (roleToShare == EcmrRole.Sender && !userRoles.contains(EcmrRole.Sender)) {
            throw new NoPermissionException("Only Sender can share with Sender");
        } else if ((roleToShare == EcmrRole.Carrier || roleToShare == EcmrRole.Consignee) && !userRoles.contains(EcmrRole.Carrier)
                && !userRoles.contains(EcmrRole.Sender)) {
            throw new NoPermissionException("Only Carrier and Sender can share with " + roleToShare.name());
        } else if (roleToShare == EcmrRole.Reader && !userRoles.contains(EcmrRole.Carrier) && !userRoles.contains(EcmrRole.Sender)
                && !userRoles.contains(EcmrRole.Consignee)) {
            throw new NoPermissionException("Reader cant share ecmr");
        }
    }

    public EcmrShareResponse shareEcmrWithGroup(InternalOrExternalUser internalOrExternalUser, @Valid @NotNull UUID ecmrId,
            @Valid @NotNull Long groupId,
            @Valid @NotNull EcmrRole role) throws EcmrNotFoundException, GroupNotFoundException, NoPermissionException, ValidationException {
        if (role == EcmrRole.Reader) {
            throw new ValidationException("Ecmr can't be shared to Reader");
        }
        List<EcmrRole> rolesOfUser = authorisationService.getRolesOfUser(internalOrExternalUser, ecmrId);
        if (!rolesOfUser.contains(EcmrRole.Sender) && !rolesOfUser.contains(EcmrRole.Carrier)) {
            throw new NoPermissionException("No Permission to share as Consignee or Readonly");
        }
        this.validateShareRoles(rolesOfUser, role);
        EcmrEntity ecmr = ecmrService.getEcmrEntity(ecmrId);

        GroupEntity groupEntity = groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));

        if (rolesOfUser.contains(role)) {
            this.changeRoleToReadonly(internalOrExternalUser, role, ecmrId);
        }
        List<EcmrAssignmentEntity> assignment = this.ecmrAssignmentRepository.findByEcmr_EcmrIdAndGroup_idInAndRole(ecmrId, List.of(groupId), role);
        if (!assignment.isEmpty()) {
            return new EcmrShareResponse(ShareEcmrResult.SharedInternal, this.groupPersistenceMapper.toGroup(groupEntity));
        }

        createAndSaveAssigment(ecmr, role, groupEntity);

        return new EcmrShareResponse(ShareEcmrResult.SharedInternal, this.groupPersistenceMapper.toGroup(groupEntity));
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
    public EcmrExportResult exportEcmrToExternal(UUID ecmrId, String shareToken)
            throws ValidationException {

        SealedDocumentEntity ecmrSealEntity = sealedDocumentService.getCurrentSealedDocument(ecmrId).orElseThrow();

        EcmrRole ecmrRole;

        if (shareToken.equals(ecmrSealEntity.getEcmr().getShareWithSenderToken())) {
            ecmrRole = EcmrRole.Sender;
        } else if (shareToken.equals(ecmrSealEntity.getEcmr().getShareWithCarrierToken())) {
            ecmrRole = EcmrRole.Carrier;
        } else if (shareToken.equals(ecmrSealEntity.getEcmr().getShareWithConsigneeToken())) {
            ecmrRole = EcmrRole.Consignee;
        } else if (shareToken.equals(ecmrSealEntity.getEcmr().getShareWithReaderToken())) {
            ecmrRole = EcmrRole.Reader;
        } else { // share token not valid
            throw new ValidationException("You need a valid share token to import this ecmr");
        }

        SealedDocument sealedDocument = sealedDocumentPersistenceMapper.toDomain(ecmrSealEntity);
        return new EcmrExportResult(sealedDocument, ecmrRole);
    }

    // import existing ecmr from external instance and save it initially on this instance
    @Transactional
    public void importEcmrFromExternal(String url, UUID ecmrId, String shareToken, List<Long> groupIds, AuthenticatedUser authenticatedUser)
            throws InvalidInputException, NoPermissionException, EcmrAlreadyExistsException, ValidationException {
        // check if the ecmr is already imported
        if (ecmrService.existsByEcmrId(ecmrId)) {
            throw new EcmrAlreadyExistsException(ecmrId);
        }

        // 1. call export endpoint from external instance
        EcmrExportResult exportResult = externalEcmrInstanceService.importEcmr(url, ecmrId, shareToken);
        // set up EcmrEntity
        SealedDocumentEntity sealedDocumentEntity = sealedDocumentPersistenceMapper.toEntity(exportResult.getSealedDocument());

        this.sealedDocumentService.validateSealedDocument(sealedDocumentEntity);

        // 2. verify sealed document before calling any internal functions
        ESeal seal = new ESeal(sealedDocumentService.getCurrentSeal(sealedDocumentEntity), null);
        if (sealedDocumentService.verify(List.of(seal)) != SealVerifyResult.VALID) {
            throw new InvalidInputException("Invalid input data");
        }

        // 3. save ecmrSealEntity

        if (!groupService.areAllGroupIdsPartOfUsersGroup(authenticatedUser, groupIds)) {
            throw new NoPermissionException("No permission for at least one group id");
        }

        // create shared token for received ecmr
        sealedDocumentEntity.getEcmr().setShareWithSenderToken(RandomStringUtils.secure().nextAlphanumeric(4));
        sealedDocumentEntity.getEcmr().setShareWithCarrierToken(RandomStringUtils.secure().nextAlphanumeric(4));
        sealedDocumentEntity.getEcmr().setShareWithConsigneeToken(RandomStringUtils.secure().nextAlphanumeric(4));
        sealedDocumentEntity.getEcmr().setShareWithReaderToken(RandomStringUtils.secure().nextAlphanumeric(4));

        sealedDocumentEntity = sealedDocumentRepository.save(sealedDocumentEntity);

        // set up group associations
        List<GroupEntity> groupEntities = groupService.getGroupEntities(groupIds);
        for (GroupEntity groupEntity : groupEntities) {
            EcmrAssignmentEntity ecmrAssignmentEntity = new EcmrAssignmentEntity();
            ecmrAssignmentEntity.setEcmr(sealedDocumentEntity.getEcmr());
            ecmrAssignmentEntity.setGroup(groupEntity);
            ecmrAssignmentEntity.setRole(exportResult.getEcmrRole());
            ecmrAssignmentRepository.save(ecmrAssignmentEntity);
        }

        // save history log
        this.historyLogService.writeHistoryLog(sealedDocumentEntity.getEcmr(), sealedDocumentEntity.getEcmr().getOriginUrl(), ActionType.Creation);
    }

    @Transactional
    public EcmrShareResponse sendTokenPerEmail(UUID ecmrId, String receiverEmail, EcmrRole ecmrRole, InternalOrExternalUser user)
            throws EcmrNotFoundException, NoPermissionException,
            SealedDocumentNotFoundException {
        // verify that a sealed document exists
        if (!sealedDocumentService.sealExists(ecmrId)) {
            throw new SealedDocumentNotFoundException(ecmrId);
        }
        String shareToken = getShareToken(ecmrId, ecmrRole, user);
        String text = String.format("To import the ecmr use the following properties: %n-ecmrId: %s %n-url: %s %n-share token: %s", ecmrId, originUrl,
                shareToken);
        mailService.sendMail(receiverEmail, "Import eCMR", text);
        return new EcmrShareResponse(ShareEcmrResult.SharedExternal, null);
    }

    public List<EcmrAssignment> getAssignmentsOfEcmr(UUID ecmrId, InternalOrExternalUser internalOrExternalUser) throws NoPermissionException {
        if (authorisationService.hasNoRole(internalOrExternalUser, ecmrId)) {
            throw new NoPermissionException("No permission to load ecmr assignments");
        }
        return this.ecmrAssignmentRepository.findByEcmr_EcmrId(ecmrId).stream().map(ecmrAssignmentMapper::map).toList();
    }
}
