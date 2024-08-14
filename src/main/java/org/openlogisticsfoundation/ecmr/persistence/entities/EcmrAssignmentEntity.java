/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.entities;

import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ECMR_ASSIGNMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EcmrAssignmentEntity extends BaseEntity {
    @OneToOne(optional = false)
    private EcmrEntity ecmr;
    @OneToOne(optional = true)
    private GroupEntity group;
    @OneToOne(optional = true)
    private ExternalUserEntity externalUser;
    @NotNull
    @Enumerated(EnumType.STRING)
    private EcmrRole role;
}
