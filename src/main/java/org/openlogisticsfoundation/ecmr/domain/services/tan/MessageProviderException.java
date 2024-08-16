/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services.tan;

public class MessageProviderException extends Exception {
    public MessageProviderException(Exception ex) {
        super(ex);
    }

    public MessageProviderException(String message) {
        super(message);
    }
}
