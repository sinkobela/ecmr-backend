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
@Table(name = "SEALED_DOCUMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SealedDocumentEntity extends BaseEntity {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ecmr_id")
    private EcmrEntity ecmr;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "sender_ecmr_seal_id")
    private EcmrSealEntity senderSeal;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "carrier_ecmr_seal_id")
    private EcmrSealEntity carrierSeal;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "successive_carrier_ecmr_seal_id")
    private EcmrSealEntity successiveCarrierSeal;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "consignee_ecmr_seal_id")
    private EcmrSealEntity consigneeSeal;
}
