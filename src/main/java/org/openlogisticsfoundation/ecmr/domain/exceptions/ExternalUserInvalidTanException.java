/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.exceptions;

public class ExternalUserInvalidTanException extends Exception {

    public ExternalUserInvalidTanException(String userToken) {
        super("Tan does not match for user with user token " + userToken);
    }

}
