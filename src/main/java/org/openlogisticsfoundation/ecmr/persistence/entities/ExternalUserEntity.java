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
@Table(name = "ECMR_EXTERNAL_USER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalUserEntity extends BaseEntity {
    private String firstName;
    private String lastName;
    private String company;
    private String email;
    private String phone;
    private String tan;
    private Instant tanValidUntil;
    private Instant creationTimestamp;
    private boolean isActive;
}
