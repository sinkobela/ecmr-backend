/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.entities;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.openlogisticsfoundation.ecmr.api.model.TransportRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ECMR_SEAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EcmrSealEntity extends BaseEntity {
    @CreationTimestamp
    private Instant created;

    @UpdateTimestamp
    private Instant last_updated;

    @Version
    private Integer version;

    @Lob
    private String seal;

    @Column(nullable = false)
    private String sealer;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransportRole transportRole;

    @Column(nullable = false)
    private Instant timestamp;


}




