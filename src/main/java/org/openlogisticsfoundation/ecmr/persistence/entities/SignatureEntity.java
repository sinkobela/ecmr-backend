/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.entities;

import java.time.Instant;

import org.openlogisticsfoundation.ecmr.domain.models.SignatureType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SIGNATURE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignatureEntity extends BaseEntity {
    @NotNull
    @Enumerated(EnumType.STRING)
    private SignatureType signatureType;
    private String userName;
    private String userCompany;
    private String userStreet;
    private String userPostCode;
    private String userCity;
    private String userCountry;
    private Instant timestamp;
    // Base64 Encoded image (Sign on Glass) or digital signature (E-Seal)
    @Lob
    private String data;
}
