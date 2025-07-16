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

import org.openlogisticsfoundation.ecmr.persistence.entities.SealedDocumentEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.SealedDocumentProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SealedDocumentRepository extends JpaRepository<SealedDocumentEntity, Long> {

    boolean existsByEcmr_EcmrId(UUID ecmrId);

    @Query("SELECT document FROM SealedDocumentEntity document WHERE document.ecmr.ecmrId = :ecmrId")
    Optional<SealedDocumentEntity> findByEcmrId(UUID ecmrId);

    @Query("SELECT document FROM SealedDocumentEntity document WHERE document.ecmr.ecmrId = :ecmrId")
    Optional<SealedDocumentProjection> findProjectionByEcmrId(UUID ecmrId);

}
