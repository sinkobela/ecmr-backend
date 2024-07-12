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
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EcmrService {
    private final EcmrRepository ecmrRepository;
    private final EcmrPersistenceMapper ecmrPersistenceMapper;

    public EcmrModel getEcmr(UUID ecmrId) throws EcmrNotFoundException {
        EcmrEntity ecmrEntity = ecmrRepository.findByEcmrId(ecmrId).orElseThrow(() -> new EcmrNotFoundException(ecmrId));
        return ecmrPersistenceMapper.toModel(ecmrEntity);
    }

    public List<EcmrModel> getAllEcmrs(EcmrType type) {
        return ecmrRepository.findAllByType(type).stream().map(ecmrPersistenceMapper::toModel).toList();
    }

    public List<EcmrModel> getAllEcmrs(EcmrType ecmrType, int page, int size, String sortBy, String sortingOrder) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortingOrder);
        final Pageable pageable = PageRequest.of(page, size, sortDirection, sortBy);

        final Page<EcmrEntity> ecmrPage = ecmrRepository.findAllByType(ecmrType, pageable);
        return ecmrPage.get()
            .map(ecmrPersistenceMapper::toModel)
            .toList();
    }

    public Integer getNumberOfEcmrsByType(final EcmrType type) {
        return getAllEcmrs(type).size();
    }

    public void deleteEcmr(UUID ecmrId) throws EcmrNotFoundException {
        EcmrEntity ecmrEntity = ecmrRepository.findByEcmrId(ecmrId).orElseThrow(() -> new EcmrNotFoundException(ecmrId));

        if (ecmrEntity != null && ecmrEntity.getCarrierInformation().getSignature() == null && ecmrEntity.getSenderInformation().getSignature() == null && ecmrEntity.getConsigneeInformation().getSignature() == null && ecmrEntity.getSuccessiveCarrierInformation().getSignature() == null && ecmrEntity.getEcmrStatus() == EcmrStatus.NEW) {
            ecmrRepository.delete(ecmrEntity);
        }
    }
}
