/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.exceptions;

import java.util.UUID;

public class SignatureNotValidException extends Exception {
    public SignatureNotValidException(UUID ecmrID, String message) {
        super("The Signature for the Ecmr with id " + ecmrID + " was not valid: " + message);
    }
}
