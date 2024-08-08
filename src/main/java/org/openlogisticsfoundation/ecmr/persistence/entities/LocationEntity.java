/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.persistence.entities;

import java.util.List;

import org.openlogisticsfoundation.ecmr.domain.models.CountryCode;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "LOCATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationEntity extends BaseEntity {
    @NotNull
    private String name;
    private String street;
    private String postcode;
    private String city;
    @Enumerated(EnumType.STRING)
    private CountryCode countryCode;
    @NotNull
    private String officeNumber;
    @OneToMany(mappedBy = "location")
    private List<GroupEntity> groups;
}
