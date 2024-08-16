/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */


package org.openlogisticsfoundation.ecmr.domain.services.tan;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DummyMessageProvider implements PhoneMessageProvider {
    @Override
    public void sendMessage(String recipientIdentifier, String message) {
        log.info("########################################");
        log.info("MESSAGE TO: '{}', MESSAGE: '{}'", recipientIdentifier, message);
        log.info("########################################");
    }
}
