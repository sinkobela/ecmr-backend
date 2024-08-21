/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.List;
import java.util.UUID;

import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrAssignmentRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EcmrService {
    private final EcmrRepository ecmrRepository;
    private final EcmrAssignmentRepository ecmrAssignmentRepository;
    private final EcmrPersistenceMapper ecmrPersistenceMapper;
    private final GroupService groupService;

    public EcmrModel getEcmr(UUID ecmrId) throws EcmrNotFoundException {
        EcmrEntity ecmrEntity = ecmrRepository.findByEcmrId(ecmrId).orElseThrow(() -> new EcmrNotFoundException(ecmrId));
        return ecmrPersistenceMapper.toModel(ecmrEntity);
    }

    EcmrEntity getEcmrEntity(UUID ecmrId) throws EcmrNotFoundException {
        return ecmrRepository.findByEcmrId(ecmrId).orElseThrow(() -> new EcmrNotFoundException(ecmrId));
    }

    public List<EcmrModel> getEcmrsForUser(AuthenticatedUser authenticatedUser, EcmrType ecmrType, int page, int size, String sortBy,
            String sortingOrder) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortingOrder);
        final Pageable pageable = PageRequest.of(page, size, sortDirection, "ecmr." + sortBy);

        List<Group> usersGroups = groupService.getGroupsForUser(authenticatedUser);
        List<Long> usersGroupIds = groupService.flatMapGroupTrees(usersGroups).stream().map(Group::getId).toList();

        final Page<EcmrEntity> ecmrPage = ecmrRepository.findAllByTypeAndAssignedGroupIds(ecmrType, usersGroupIds, pageable);

        return ecmrPage.get()
                .map(ecmrPersistenceMapper::toModel)
                .toList();
    }

    @Transactional
    public void deleteEcmr(UUID ecmrId) throws EcmrNotFoundException, ValidationException {
        EcmrEntity ecmrEntity = ecmrRepository.findByEcmrId(ecmrId).orElseThrow(() -> new EcmrNotFoundException(ecmrId));

        if (ecmrEntity == null || ecmrEntity.getCarrierInformation().getSignature() != null
                || ecmrEntity.getSenderInformation().getSignature() != null
                || ecmrEntity.getConsigneeInformation().getSignature() != null
                || ecmrEntity.getSuccessiveCarrierInformation().getSignature() != null
                || ecmrEntity.getEcmrStatus() != EcmrStatus.NEW) {
            throw new ValidationException("Ecmr can not be deleted");
        }
        ecmrAssignmentRepository.deleteByEcmr_EcmrId(ecmrId);
        ecmrRepository.delete(ecmrEntity);
    }

    public String getShareToken(UUID ecmrId, EcmrRole ecmrRole) throws EcmrNotFoundException, ValidationException {
        if(ecmrRole == EcmrRole.Reader) {
            throw new ValidationException("No token required  for reader");
        }
        EcmrEntity ecmrEntity = ecmrRepository.findByEcmrId(ecmrId).orElseThrow(() -> new EcmrNotFoundException(ecmrId));
        return switch (ecmrRole) {
            case Sender -> ecmrEntity.getShareWithSenderToken();
            case Consignee -> ecmrEntity.getShareWithConsigneeToken();
            case Carrier -> ecmrEntity.getShareWithCarrierToken();
            default -> throw new ValidationException("Unexpected value: " + ecmrRole);
        };
    }
}
