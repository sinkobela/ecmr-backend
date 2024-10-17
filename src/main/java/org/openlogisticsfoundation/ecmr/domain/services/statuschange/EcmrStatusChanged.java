/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services.statuschange;

import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;

/**
 * To do something when a status change is happening, implement this. All implementations will be called, after the status changes is saved.
 */
public interface EcmrStatusChanged {
    /**
     * @param previousStatus status of the ecmr before the status change
     * @param ecmrEntity ecmrEntity after the status change
     */
    void onEcmrStatusChange(EcmrStatus previousStatus, EcmrEntity ecmrEntity) throws EcmrStatusChangedException;
}
