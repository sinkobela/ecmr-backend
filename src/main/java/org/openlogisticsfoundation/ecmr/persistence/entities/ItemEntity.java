/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ITEM", indexes = {
        @Index(columnList = "ecmr_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemEntity extends BaseEntity {
    //Marks and Nos
    private String logisticsShippingMarksMarking;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private List<LogisticsShippingMarksCustomBarcodeEntity> logisticsShippingMarksCustomBarcodeList;
    //Number of Packages
    private Integer logisticsPackageItemQuantity;
    //Method of Packing
    private String logisticsPackageType;
    //Nature of the Goods
    private String transportCargoIdentification;
    //Gross Weight in KG
    private Float supplyChainConsignmentItemGrossWeight;
    //Volume In m³
    private Float supplyChainConsignmentItemGrossVolume;

    @Override
    public String toString() {
        return logisticsShippingMarksMarking + logisticsShippingMarksCustomBarcodeList.toString() + logisticsPackageItemQuantity + logisticsPackageType
                + transportCargoIdentification + supplyChainConsignmentItemGrossWeight + supplyChainConsignmentItemGrossVolume;
    }
}
