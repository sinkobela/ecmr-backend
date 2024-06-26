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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class EcmrMemberEntity extends BaseEntity {
    private String nameCompany;
    private String namePerson;
    private String street;
    private String postcode;
    private String city;
    private String countryCode;
    private String email;
    private String phone;
    @Valid
    @JoinColumn(name = "signature_id")
    @ManyToOne(cascade = CascadeType.ALL) //ManyToOne annotation is necessary because OneToOne can't be optional
    private SignatureEntity signature;
}
