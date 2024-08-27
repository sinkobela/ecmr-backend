/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.persistence.entities;

import org.openlogisticsfoundation.ecmr.domain.models.CountryCode;
import org.openlogisticsfoundation.ecmr.domain.models.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ECMR_USER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity {
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    @Enumerated(EnumType.STRING)
    private CountryCode country;
    @NotNull
    @Column(unique = true)
    private String email;
    private String phone;
    @NotNull
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @ManyToOne
    @JoinColumn(name = "default_group_id")
    private GroupEntity defaultGroup;
    private boolean deactivated = false;
}
