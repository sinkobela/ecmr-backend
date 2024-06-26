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
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TakingOverTheGoodsCommand {
    @Size(min = 2, max = 60)
    private String takingOverTheGoodsPlace;
    private Instant logisticsTimeOfArrivalDateTime;
    private Instant logisticsTimeOfDepartureDateTime;
}
