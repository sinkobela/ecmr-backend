/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.UUID;

import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrAssignmentRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrRepository;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EcmrTanService {
    private final EcmrRepository ecmrRepository;
    private final EcmrAssignmentRepository ecmrAssignmentRepository;
    private final EcmrPersistenceMapper ecmrPersistenceMapper;

    public EcmrModel getEcmrWithTan(UUID ecmrId, String tan) throws EcmrNotFoundException, ValidationException {
        if(!this.isTanValid(ecmrId, tan)){
            throw new ValidationException("No valid Tan given");
        }
        EcmrEntity ecmrEntity = ecmrRepository.findByEcmrId(ecmrId).orElseThrow(() -> new EcmrNotFoundException(ecmrId));
        return ecmrPersistenceMapper.toModel(ecmrEntity);
    }

    public boolean isTanValid(@Valid @NotNull UUID ecmrId, @Valid @NotNull String tan) throws EcmrNotFoundException {
        if (!ecmrRepository.existsByEcmrId(ecmrId)) {
            throw new EcmrNotFoundException(ecmrId);
        }
        return ecmrAssignmentRepository.existsByEcmr_EcmrIdAndExternalUser_Tan(ecmrId, tan);
    }
}
