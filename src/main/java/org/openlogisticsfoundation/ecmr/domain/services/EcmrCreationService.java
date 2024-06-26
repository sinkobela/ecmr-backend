/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.services;

import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EcmrCreationService {
    private final EcmrPersistenceMapper persistenceMapper;
    private final EcmrRepository ecmrRepository;

    public void createEcmr(EcmrCommand ecmrCommand) {
        this.createEcmr(ecmrCommand, EcmrType.ECMR);
    }

    public void createTemplate(EcmrCommand ecmrCommand) {
        this.createEcmr(ecmrCommand, EcmrType.TEMPLATE);
    }

    private void createEcmr(EcmrCommand ecmrCommand, EcmrType type) {
        EcmrEntity ecmrEntity = this.persistenceMapper.toEntity(ecmrCommand, type);
        ecmrEntity = this.ecmrRepository.save(ecmrEntity);
    }
}
