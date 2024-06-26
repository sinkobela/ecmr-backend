/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.models.commands;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ItemCommand {
    @Size(min = 2, max = 512)
    private String logisticsShippingMarksMarking;
    @Size(min = 2, max = 35)
    private String logisticsShippingMarksCustomBarcode;
    @Min(0)
    @Max(9999)
    private Integer logisticsPackageItemQuantity;
    @Size(min = 2, max = 35)
    private String logisticsPackageType;
    @Size(min = 2, max = 512)
    private String transportCargoIdentification;
    @Min(1)
    @Max(99_999)
    private Integer supplyChainConsignmentItemGrossWeight;
    @Min(1)
    @Max(9_999)
    private float supplyChainConsignmentItemGrossVolume;

}
