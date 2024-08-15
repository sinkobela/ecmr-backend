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

import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query("SELECT ea.ecmr FROM EcmrAssignmentEntity ea WHERE ea.group.id in :groupIds AND ea.ecmr.type = :type")
    Page<EcmrEntity> findAllByTypeAndAssignedGroupIds(@Param("type") EcmrType type, @Param("groupIds") List<Long> groupIds, Pageable pageable);

    boolean existsByEcmrId(UUID ecmrId);
}
