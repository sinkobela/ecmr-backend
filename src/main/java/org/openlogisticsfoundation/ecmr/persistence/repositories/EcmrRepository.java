/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.persistence.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.openlogisticsfoundation.ecmr.domain.models.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EcmrRepository extends JpaRepository<EcmrEntity, Long> {
    @EntityGraph(value = "Ecmr.all", type = EntityGraph.EntityGraphType.FETCH)
    Optional<EcmrEntity> findByEcmrId(UUID ecmrId);

    @EntityGraph(value = "Ecmr.all", type = EntityGraph.EntityGraphType.FETCH)
    List<EcmrEntity> findAllByType(EcmrType type);

    @EntityGraph(value = "Ecmr.all", type = EntityGraph.EntityGraphType.FETCH)
    List<EcmrEntity> findAllByEcmrStatusAndType(EcmrStatus ecmrStatus, EcmrType type);

    @EntityGraph(value = "Ecmr.all", type = EntityGraph.EntityGraphType.FETCH)
    Page<EcmrEntity> findAllByType(EcmrType type, Pageable pageable);
}
