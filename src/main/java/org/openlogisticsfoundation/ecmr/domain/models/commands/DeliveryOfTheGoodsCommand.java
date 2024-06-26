/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.models.commands;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliveryOfTheGoodsCommand {
    @Size(min = 2, max = 60)
    private String logisticsLocationCity;
    @Size(min = 2, max = 255)
    private String logisticsLocationOpeningHours;
}
