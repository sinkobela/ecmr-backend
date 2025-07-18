/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.models.commands;

import java.time.Instant;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class GoodsReceivedCommand {
    @Size(min = 2, max = 60)
    private String confirmedLogisticsLocationName;
    @Size(min = 2, max = 512)
    private String consigneeReservationsObservations;
    private Instant consigneeSignatureDate;
    private Instant consigneeTimeOfArrival;
    private Instant consigneeTimeOfDeparture;
}
