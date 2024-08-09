/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.persistence.repositories;

import java.util.List;

import org.openlogisticsfoundation.ecmr.persistence.entities.LocationEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserToLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserToLocationRepository extends JpaRepository<UserToLocationEntity, Long> {
    @Query("SELECT u.user FROM UserToLocationEntity u WHERE u.location.id = :locationId")
    List<UserEntity> findUsersByLocationId(@Param("locationId") long locationId);

    @Query("SELECT u.location FROM UserToLocationEntity u WHERE u.user.id = :userId")
    List<LocationEntity> findLocationsByUserId(@Param("userId") long userId);

    void deleteByUserIdAndLocationId(long userId, long locationId);
}
