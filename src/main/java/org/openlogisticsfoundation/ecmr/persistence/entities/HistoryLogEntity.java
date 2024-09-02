/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.persistence.entities;

import java.time.Instant;

import org.openlogisticsfoundation.ecmr.domain.models.ActionType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "HISTORY_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoryLogEntity extends BaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ecmr_id")
    private EcmrEntity ecmr;

    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    private String actionFrom;

    private Instant timestamp;
}
