/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services.statuschange;

public class EcmrStatusChangedException extends Exception {
    public EcmrStatusChangedException(Exception ex) {
        super(ex);
    }

    public EcmrStatusChangedException(String message) {
        super(message);
    }
}
