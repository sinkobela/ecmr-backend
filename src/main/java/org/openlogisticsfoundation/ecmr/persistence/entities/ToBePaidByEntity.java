/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TO_BE_PAID_BY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ToBePaidByEntity extends BaseEntity {
    @JoinColumn(name = "custom_charge_carriage_id")
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private CustomChargeEntity customChargeCarriage;
    @JoinColumn(name = "custom_charge_customs_duties_id")
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private CustomChargeEntity customChargeCustomsDuties;
    @JoinColumn(name = "custom_charge_other_id")
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private CustomChargeEntity customChargeOther;
    @JoinColumn(name = "custom_charge_supplementary_id")
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private CustomChargeEntity customChargeSupplementary;
}
