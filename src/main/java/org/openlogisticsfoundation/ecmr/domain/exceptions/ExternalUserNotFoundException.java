/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.exceptions;

public class ExternalUserNotFoundException extends Exception {

    public ExternalUserNotFoundException(String tan) {
        super("User with tan " + tan + " not found");
    }

}
