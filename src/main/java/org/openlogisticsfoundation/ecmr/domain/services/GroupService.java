/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.List;

import org.openlogisticsfoundation.ecmr.domain.exceptions.LocationNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.mappers.GroupPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.domain.models.commands.GroupCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.GroupEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.LocationEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.GroupRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.LocationRepository;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupPersistenceMapper groupPersistenceMapper;
    private final LocationRepository locationRepository;

    public List<Group> getAllGroups() {
        return groupRepository.findAll().stream().map(groupPersistenceMapper::toGroup).toList();
    }

    public Group createGroup(@Valid GroupCommand command) throws LocationNotFoundException {
        LocationEntity location = locationRepository.findById(command.getLocationId())
                .orElseThrow(() -> new LocationNotFoundException(command.getLocationId()));
        GroupEntity group = new GroupEntity(command.getName().trim(), location);
        return groupPersistenceMapper.toGroup(groupRepository.save(group));
    }

    public List<Group> getGroupsByLocationId(long locationId) {
        return groupRepository.findByLocation_Id(locationId).stream().map(groupPersistenceMapper::toGroup).toList();
    }
}
