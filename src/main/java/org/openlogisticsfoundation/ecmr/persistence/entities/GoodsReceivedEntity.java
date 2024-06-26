/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.entities;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GOODS_RECEIVED")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceivedEntity extends BaseEntity {
    private String confirmedLogisticsLocationName;
    private String consigneeReservationsObservations;
    private Instant consigneeTimeOfArrival;
    private Instant consigneeTimeOfDeparture;
}
