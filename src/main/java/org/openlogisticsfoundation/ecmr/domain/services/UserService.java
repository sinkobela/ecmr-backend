/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.List;

import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.mappers.GroupPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.mappers.LocationPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.mappers.UserPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.CountryCode;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.domain.models.Location;
import org.openlogisticsfoundation.ecmr.domain.models.User;
import org.openlogisticsfoundation.ecmr.domain.models.commands.UserCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.GroupEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.LocationEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserToGroupEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserToLocationEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.GroupRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.LocationRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.UserRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.UserToGroupRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.UserToLocationRepository;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPersistenceMapper userPersistenceMapper;
    private final LocationRepository locationRepository;
    private final GroupRepository groupRepository;
    private final UserToGroupRepository userToGroupRepository;
    private final UserToLocationRepository userToLocationRepository;
    private final GroupPersistenceMapper groupPersistenceMapper;
    private final LocationPersistenceMapper locationPersistenceMapper;

    public List<User> getAllUsers() {
        return userRepository.findAll().stream().map(userPersistenceMapper::toUser).toList();
    }

    public User createUser(@Valid UserCommand userCommand) throws ValidationException {
        UserEntity userEntity = userPersistenceMapper.toUserEntity(userCommand);
        userEntity = userRepository.save(userEntity);
        List<LocationEntity> locations = locationRepository.findAllById(userCommand.getLocationIds());
        List<GroupEntity> groups = groupRepository.findAllById(userCommand.getGroupIds());
        this.validateLocationsAndGroups(userCommand.getCountry(), locations, groups);

        for (GroupEntity groupEntity : groups) {
            UserToGroupEntity userToGroupEntity = new UserToGroupEntity(userEntity, groupEntity);
            this.userToGroupRepository.save(userToGroupEntity);
        }

        for (LocationEntity locationEntity : locations) {
            UserToLocationEntity userToLocationEntity = new UserToLocationEntity(userEntity, locationEntity);
            this.userToLocationRepository.save(userToLocationEntity);
        }

        return userPersistenceMapper.toUser(userEntity);
    }

    public List<Group> getGroupsByUserId(Long userId) {
        return userToGroupRepository.findGroupsByUserId(userId).stream().map(groupPersistenceMapper::toGroup).toList();
    }

    public List<Location> getLocationsByUserId(Long userId) {
        return userToLocationRepository.findLocationsByUserId(userId).stream().map(locationPersistenceMapper::toLocation).toList();
    }

    private void validateLocationsAndGroups(CountryCode userCountry, List<LocationEntity> locationEntities, List<GroupEntity> groupEntities) throws ValidationException {
        if (locationEntities.stream().anyMatch(l -> l.getCountryCode() != userCountry)) {
            throw new ValidationException("At least one location has not the same country code as the user");
        }
        if (groupEntities.stream().anyMatch(g -> !locationEntities.contains(g.getLocation()))) {
            throw new ValidationException("At least one group is not part of the users locations");
        }
    }

    public List<User> getUsersByGroupId(long groupId) {
        return userToGroupRepository.findUsersByGroupId(groupId).stream().map(userPersistenceMapper::toUser).toList();
    }

    public List<User> getUsersByLocationId(long locationId) {
        return userToGroupRepository.findUsersByGroupId(locationId).stream().map(userPersistenceMapper::toUser).toList();
    }
}
