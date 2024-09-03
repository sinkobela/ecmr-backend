/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "API_KEY")
@Getter
@Setter
@NoArgsConstructor
@NamedEntityGraph(name = "ApiKey.all",
        attributeNodes = { @NamedAttributeNode("user") })
public class ApiKeyEntity extends BaseEntity {
    @Column(nullable = false, unique = true)
    private UUID value;
    @Column(nullable = false, unique = true)
    private String description;
    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne
    private UserEntity user;
}
