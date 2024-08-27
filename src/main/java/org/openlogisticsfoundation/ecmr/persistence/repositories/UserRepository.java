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

import org.openlogisticsfoundation.ecmr.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    List<UserEntity> findAllByDeactivatedFalse();
    Optional<UserEntity> findByEmailAndDeactivatedFalse(String email);
}
