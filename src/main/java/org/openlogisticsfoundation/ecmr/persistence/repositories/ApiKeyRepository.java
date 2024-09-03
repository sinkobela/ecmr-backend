/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.repositories;

import java.util.Optional;
import java.util.UUID;

import org.openlogisticsfoundation.ecmr.persistence.entities.ApiKeyEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {
    @EntityGraph(value = "ApiKey.all", type = EntityGraph.EntityGraphType.FETCH)
    Optional<ApiKeyEntity> findByValue(UUID value);
}
