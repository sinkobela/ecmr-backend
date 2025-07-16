/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.services;

import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.ActionType;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrAssignmentEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.GroupEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrAssignmentRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EcmrCreationService {
    private final EcmrPersistenceMapper persistenceMapper;
    private final EcmrRepository ecmrRepository;
    private final EcmrAssignmentRepository ecmrAssignmentRepository;
    private final GroupService groupService;
    private final AuthorisationService authorisationService;
    private final EcmrService ecmrService;
    private final HistoryLogService historyLogService;
    private final EcmrStatusService ecmrStatusService;

    @Value("${app.origin.url}")
    private String originUrl;

    public EcmrModel createEcmr(EcmrCommand ecmrCommand, AuthenticatedUser authenticatedUser, List<Long> groupIds)
            throws NoPermissionException {
        if (!groupService.areAllGroupIdsPartOfUsersGroup(authenticatedUser, groupIds)) {
            throw new NoPermissionException("No permission for at least one group id");
        }
        if (!authorisationService.validateSaveCommand(ecmrCommand)) {
            throw new NoPermissionException("Save command is not valid");
        }
        List<GroupEntity> groupEntities = groupService.getGroupEntities(groupIds);
        EcmrEntity ecmrEntity = this.createEcmr(ecmrCommand, EcmrType.ECMR, authenticatedUser);
        for (GroupEntity groupEntity : groupEntities) {
            EcmrAssignmentEntity ecmrAssignmentEntity = new EcmrAssignmentEntity();
            ecmrAssignmentEntity.setEcmr(ecmrEntity);
            ecmrAssignmentEntity.setGroup(groupEntity);
            ecmrAssignmentEntity.setRole(EcmrRole.Sender);
            ecmrAssignmentRepository.save(ecmrAssignmentEntity);
        }
        EcmrEntity entity = this.ecmrStatusService.setEcmrStatus(ecmrEntity, new InternalOrExternalUser(authenticatedUser.getUser()));
        return persistenceMapper.toModel(entity);
    }

    public EcmrEntity createTemplate(EcmrCommand ecmrCommand, AuthenticatedUser authenticatedUser) {
        return this.createEcmr(ecmrCommand, EcmrType.TEMPLATE, authenticatedUser);
    }

    private EcmrEntity createEcmr(EcmrCommand ecmrCommand, EcmrType type, AuthenticatedUser authenticatedUser) {
        EcmrEntity ecmrEntity = this.persistenceMapper.toEntity(ecmrCommand, type, EcmrStatus.NEW);
        ecmrEntity.setShareWithSenderToken(RandomStringUtils.secure().nextAlphanumeric(4));
        ecmrEntity.setShareWithCarrierToken(RandomStringUtils.secure().nextAlphanumeric(4));
        ecmrEntity.setShareWithConsigneeToken(RandomStringUtils.secure().nextAlphanumeric(4));
        ecmrEntity.setShareWithReaderToken(RandomStringUtils.secure().nextAlphanumeric(4));

        String fullName = String.format("%s %s", authenticatedUser.getUser().getFirstName(), authenticatedUser.getUser().getLastName());
        ecmrEntity.setCreatedBy(fullName);
        ecmrEntity.setCreatedAt(Instant.now());
        ecmrEntity.setOriginUrl(originUrl);

        ecmrEntity = ecmrService.clearPhoneNumbers(ecmrEntity);

        ecmrEntity = this.ecmrRepository.save(ecmrEntity);

        if (type == EcmrType.ECMR) {
            this.historyLogService.writeHistoryLog(ecmrEntity, fullName, ActionType.Creation);
        }

        return ecmrEntity;
    }
}
