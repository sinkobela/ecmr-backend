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

import org.openlogisticsfoundation.ecmr.persistence.entities.ExternalUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalUserRepository extends JpaRepository<ExternalUserEntity, Long> {
    @Query("SELECT e FROM ExternalUserEntity e Inner Join EcmrAssignmentEntity a on e.id = a.externalUser.id "
            + "WHERE a.ecmr.ecmrId = :ecmrId AND e.userToken = :userToken AND e.isActive = true")
    Optional<ExternalUserEntity> findExtenalUserByUserTokenAndEcmrId(String userToken,  UUID ecmrId);

    List<ExternalUserEntity> findByPhone(String phone);
}
