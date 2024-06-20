/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "DELIVERY_OF_THE_GOODS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryOfTheGoodsEntity extends BaseEntity {
    private String logisticsLocationCity;
    private String logisticsLocationOpeningHours;
}
