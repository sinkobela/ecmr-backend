/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SEALED_ECMR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SealedEcmrEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ecmr_id")
    private EcmrEntity ecmr;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ecmr_sealing_metadata_entity_id")
    private EcmrSealingMetadataEntity metadata;
}
