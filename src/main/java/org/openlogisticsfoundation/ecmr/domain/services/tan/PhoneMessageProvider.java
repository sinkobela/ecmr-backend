/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services.tan;

/// To use a custom message provider instead of the DummyMessageProvider that logs to console, implement this interface and annotate your class with @Service and @Primary
public interface PhoneMessageProvider {
    void sendMessage(String recipientIdentifier, String message) throws MessageProviderException;
}
