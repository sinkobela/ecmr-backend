/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.List;

import org.openlogisticsfoundation.ecmr.domain.mappers.GroupPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.GroupModel;
import org.openlogisticsfoundation.ecmr.persistence.entities.GroupEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.LocationEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.GroupRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.LocationRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupPersistenceMapper groupPersistenceMapper;
    private final LocationRepository locationRepository;

    public List<GroupModel> getAllGroups() {
        return groupRepository.findAll().stream().map(groupPersistenceMapper::toModel).toList();
    }

    public List<GroupModel> getAllGroupsForCurrentUser(List<LocationEntity> locationsOfUser) {
        return groupRepository.findAllByLocationIn(locationsOfUser).stream().map(groupPersistenceMapper::toModel).toList();
    }

    public GroupModel createGroup(String name, Long locationId) {
        LocationEntity location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        GroupEntity group = new GroupEntity(name, location);
        return groupPersistenceMapper.toModel(groupRepository.save(group));
    }
}
