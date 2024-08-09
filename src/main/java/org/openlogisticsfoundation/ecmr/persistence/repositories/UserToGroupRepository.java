/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.persistence.repositories;

import java.util.List;

import org.openlogisticsfoundation.ecmr.persistence.entities.GroupEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserToGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserToGroupRepository extends JpaRepository<UserToGroupEntity, Long> {
    @Query("SELECT u.user FROM UserToGroupEntity u WHERE u.group.id = :groupId")
    List<UserEntity> findUsersByGroupId(@Param("groupId") long groupId);

    @Query("SELECT u.group FROM UserToGroupEntity u WHERE u.user.id = :userId")
    List<GroupEntity> findGroupsByUserId(@Param("userId") long userId);

    void deleteByUserIdAndGroupId(long userId, long groupId);
}
