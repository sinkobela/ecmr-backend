/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.exceptions;

import java.util.UUID;

import org.openlogisticsfoundation.ecmr.api.model.TransportRole;

public class SealAlreadyPresentException extends Exception {
    public SealAlreadyPresentException(UUID ecmrID, TransportRole role) {
        super("Ecmr with id " + ecmrID + " already has the seal of role " + role);
    }
}
