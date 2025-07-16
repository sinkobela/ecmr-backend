/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services.statuschange;

import java.util.List;

import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@AllArgsConstructor
@Log4j2
public class EcmrStatusChangedService {

    private List<EcmrStatusChanged> ecmrStatusChangedList;

    public void ecmrStatusChanged(EcmrStatus previousStatus, EcmrEntity ecmrEntity, InternalOrExternalUser user) {
        if (ecmrStatusChangedList.isEmpty() || ecmrEntity.getEcmrStatus().equals(previousStatus)) {
            return;
        }

        for (EcmrStatusChanged ecmrStatusChanged : ecmrStatusChangedList) {
            try {
                ecmrStatusChanged.onEcmrStatusChange(previousStatus, ecmrEntity, user);
            } catch (EcmrStatusChangedException e) {
                log.error("Error while doing Status change: {}", e.getMessage());
                log.debug(e);
            }
        }
    }
}
