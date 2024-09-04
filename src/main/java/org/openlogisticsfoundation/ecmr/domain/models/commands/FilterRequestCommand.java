/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.models.commands;

import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrTransportType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FilterRequestCommand {
    private String referenceId;
    private String from;
    private String to;
    private EcmrTransportType transportType;
    private EcmrStatus status;
    private String licensePlate;
    private String carrierName;
    private String carrierPostCode;
    private String consigneePostCode;
    private String lastEditor;
}
