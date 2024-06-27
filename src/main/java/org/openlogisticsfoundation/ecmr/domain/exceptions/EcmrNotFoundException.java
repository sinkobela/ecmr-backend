/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.exceptions;

import java.util.UUID;

public class EcmrNotFoundException extends Exception {
    public EcmrNotFoundException(UUID ecmrId) {
        super("Ecmr with id " + ecmrId + " not found");
    }
}
