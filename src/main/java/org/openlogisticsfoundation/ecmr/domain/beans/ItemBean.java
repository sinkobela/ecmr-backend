/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public @Getter
@Setter
@NoArgsConstructor
class ItemBean {
    private String logisticsShippingMarksMarking;
    private String logisticsShippingMarksCustomBarcode;
    private Integer logisticsPackageItemQuantity;
    private String logisticsPackageType;
    private String transportCargoIdentification;
    private Integer supplyChainConsignmentItemGrossWeight;
    private Float supplyChainConsignmentItemGrossVolume;
}
