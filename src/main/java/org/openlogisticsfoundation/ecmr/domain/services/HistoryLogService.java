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
import java.util.UUID;
import java.util.stream.Collectors;

import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.mappers.HistoryLogPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.ActionType;
import org.openlogisticsfoundation.ecmr.domain.models.HistoryLog;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.HistoryLogEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.HistoryLogRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoryLogService {
    private final HistoryLogRepository historyLogRepository;
    private final HistoryLogPersistenceMapper historyLogPersistenceMapper;
    private final AuthorisationService authorisationService;

    public List<HistoryLog> getLogs(UUID ecmrId, InternalOrExternalUser internalOrExternalUser) throws NoPermissionException {
        if (authorisationService.hasNoRole(internalOrExternalUser, ecmrId)) {
            throw new NoPermissionException("No permission to load history of ecmr");
        }

        List<HistoryLogEntity> historyLogEntities = historyLogRepository.findByEcmr_EcmrIdOrderByTimestampDesc(ecmrId);

        return historyLogEntities.stream()
                .map(historyLogPersistenceMapper::toModel)
                .collect(Collectors.toList());
    }

    public void writeHistoryLog(EcmrEntity ecmr, String fullName, ActionType actionType) {
        HistoryLogEntity historyLog = new HistoryLogEntity();
        historyLog.setActionFrom(fullName);
        historyLog.setEcmr(ecmr);
        historyLog.setActionType(actionType);
        historyLog.setTimestamp(Instant.now());

        historyLogRepository.save(historyLog);
    }
}
