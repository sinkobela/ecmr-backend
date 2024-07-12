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

import org.openlogisticsfoundation.ecmr.persistence.entities.TemplateUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateUserRepository extends JpaRepository<TemplateUserEntity, Long> {
    List<TemplateUserEntity> findAllByEcmr_CreatedBy(String userName);

    Optional<TemplateUserEntity> findByIdAndEcmr_CreatedBy(Long id, String userName);

    @Query("SELECT MAX(t.templateUserNumber) FROM TemplateUserEntity t JOIN EcmrEntity e ON t.ecmr.id = e.id WHERE e.createdBy = :createdByUser")
    Integer findMaxTemplateNumberForUser(@Param("createdByUser") String createdByUser);
}
