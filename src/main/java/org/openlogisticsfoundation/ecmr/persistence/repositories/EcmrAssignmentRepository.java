/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.repositories;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EcmrAssignmentRepository extends JpaRepository<EcmrAssignmentEntity, Long> {
    boolean existsByEcmr_EcmrIdAndExternalUser_TanAndExternalUser_IsActiveTrue(UUID ecmrId, String externalUserTan);
    void deleteByEcmr_EcmrId(UUID ecmrId);
    List<EcmrAssignmentEntity> findByEcmr_EcmrIdAndGroup_IdIn(UUID ecmrId, List<Long> groupIds);
    List<EcmrAssignmentEntity> findByEcmr_EcmrIdAndExternalUser_TanAndExternalUser_IsActiveTrue(UUID ecmrId, String externalUserTan);
    List<EcmrAssignmentEntity> findByGroup_Id(long id);
    List<EcmrAssignmentEntity> findByEcmr_EcmrIdAndGroup_idInAndRole(UUID ecmrId, List<Long> groupIds, EcmrRole role);
    List<EcmrAssignmentEntity> findByEcmr_EcmrIdAndExternalUser_TanAndRoleAndExternalUser_IsActiveTrue(UUID ecmrId, String externalUserTan, EcmrRole role);
    int countByEcmr_EcmrIdAndExternalUser_CreationTimestampGreaterThan(UUID ecmrId, Instant creationTimestamp);
}
