/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "USER_TO_ECMR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserToEcmrEntity extends BaseEntity{
    @OneToOne
    private UserEntity user;
    @OneToOne
    private EcmrEntity ecmr;
}
