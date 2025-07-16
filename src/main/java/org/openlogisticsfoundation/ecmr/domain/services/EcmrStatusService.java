/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.Optional;

import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.services.statuschange.EcmrStatusChangedService;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.SealedDocumentEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.SealedDocumentRepository;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@AllArgsConstructor
@Log4j2
public class EcmrStatusService {

    private final EcmrRepository ecmrRepository;
    private final EcmrStatusChangedService ecmrStatusChangedService;
    private final SealedDocumentRepository sealedDocumentRepository;

    public EcmrEntity setEcmrStatus(EcmrEntity ecmrEntity) {
        Optional<SealedDocumentEntity> sealedDocument = sealedDocumentRepository.findByEcmrId(ecmrEntity.getEcmrId());
        if (sealedDocument.isPresent()) {
            return this.setEcmrStatus(sealedDocument.get());
        }
        EcmrStatus initialState = ecmrEntity.getEcmrStatus();
        ecmrEntity.setEcmrStatus(EcmrStatus.NEW);
        ecmrEntity = ecmrRepository.save(ecmrEntity);
        ecmrStatusChangedService.ecmrStatusChanged(initialState, ecmrEntity);
        return ecmrEntity;
    }

    public EcmrEntity setEcmrStatus(SealedDocumentEntity sealedDocumentEntity) {
        EcmrEntity ecmrEntity = sealedDocumentEntity.getEcmr();
        EcmrStatus initialState = ecmrEntity.getEcmrStatus();
        ecmrEntity.setEcmrStatus(EcmrStatus.NEW);
        if (sealedDocumentEntity.getSenderSeal() != null) {
            ecmrEntity.setEcmrStatus(EcmrStatus.LOADING);
        }
        if (sealedDocumentEntity.getCarrierSeal() != null) {
            ecmrEntity.setEcmrStatus(EcmrStatus.IN_TRANSPORT);
        }
        if (sealedDocumentEntity.getConsigneeSeal() != null) {
            ecmrEntity.setEcmrStatus(EcmrStatus.DELIVERED);
        }
        ecmrEntity = ecmrRepository.save(ecmrEntity);
        ecmrStatusChangedService.ecmrStatusChanged(initialState, ecmrEntity);
        return ecmrEntity;
    }
}
