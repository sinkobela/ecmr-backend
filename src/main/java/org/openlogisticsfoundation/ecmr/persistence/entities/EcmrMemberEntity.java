/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import jakarta.validation.Valid;

@MappedSuperclass
public class EcmrMemberEntity extends BaseEntity {
    private String companyName;
    private String personName;
    private String street;
    private String postCode;
    private String city;
    //TODO: Add countrycode based on decision, enum or entity
    private String email;
    private String phone;
    @Valid
    @JoinColumn(name = "signature_id")
    @OneToOne(cascade = CascadeType.ALL)
    private SignatureEntity signatureEntity;
}
