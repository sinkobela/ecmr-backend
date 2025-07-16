/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.models.commands;

import java.util.List;

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
    private List<LogisticsShippingMarksCustomBarcodeCommand> logisticsShippingMarksCustomBarcodeList;
    @Min(0)
    @Max(9999)
    private Integer logisticsPackageItemQuantity;
    @Size(min = 2, max = 35)
    private String logisticsPackageType;
    @Size(min = 2, max = 512)
    private String transportCargoIdentification;
    @Min(1)
    @Max(99_999)
    private Float supplyChainConsignmentItemGrossWeight;
    @Min(1)
    @Max(9_999)
    private Float supplyChainConsignmentItemGrossVolume;

    @Override
    public String toString() {
        return logisticsShippingMarksMarking + logisticsShippingMarksCustomBarcodeList.toString() + logisticsPackageItemQuantity + logisticsPackageType
                + transportCargoIdentification + supplyChainConsignmentItemGrossWeight + supplyChainConsignmentItemGrossVolume;
    }
}
