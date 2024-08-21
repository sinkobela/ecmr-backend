/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.repositories;

import java.util.UUID;

import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EcmrAssignmentRepository extends JpaRepository<EcmrAssignmentEntity, Long> {
    boolean existsByEcmr_EcmrIdAndExternalUser_Tan(UUID ecmrId, String externalUserTan);
    void deleteByEcmr_EcmrId(UUID ecmrId);
}
