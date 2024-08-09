/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.ArrayList;
import java.util.List;

import org.openlogisticsfoundation.ecmr.domain.exceptions.GroupNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.LocationNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.UserNotFoundException;
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
import org.springframework.transaction.annotation.Transactional;

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

    public User createUser(@Valid UserCommand userCommand) throws ValidationException, LocationNotFoundException, GroupNotFoundException {
        UserEntity userEntity = userPersistenceMapper.toUserEntity(userCommand);
        userEntity = userRepository.save(userEntity);
        List<LocationEntity> locations = new ArrayList<>();
        for (long locationId : userCommand.getLocationIds()) {
            locations.add(locationRepository.findById(locationId).orElseThrow(() -> new LocationNotFoundException(locationId)));
        }
        List<GroupEntity> groups = new ArrayList<>();
        for (long groupId : userCommand.getGroupIds()) {
            groups.add(groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId)));
        }
        this.validateLocationsAndGroups(userCommand.getCountry(), locations, groups);

        addGroupsAndLocationsToUser(userEntity, groups, locations);
        return userPersistenceMapper.toUser(userEntity);
    }

    public List<Group> getGroupsByUserId(Long userId) {
        return userToGroupRepository.findGroupsByUserId(userId).stream().map(groupPersistenceMapper::toGroup).toList();
    }

    public List<Location> getLocationsByUserId(Long userId) {
        return userToLocationRepository.findLocationsByUserId(userId).stream().map(locationPersistenceMapper::toLocation).toList();
    }

    @Transactional
    public User updateUser(long userId, UserCommand userCommand) throws UserNotFoundException, GroupNotFoundException, LocationNotFoundException, ValidationException {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        userEntity = userPersistenceMapper.toUserEntity(userEntity, userCommand);

        // Get the current groups and locations of the user
        List<GroupEntity> groups = userToGroupRepository.findGroupsByUserId(userId);
        List<LocationEntity> locations = userToLocationRepository.findLocationsByUserId(userId);

        // Get a list of groups and locations that need to be deleted
        List<GroupEntity> groupsToDelete = groups.stream()
                .filter(group -> !userCommand.getGroupIds().contains(group.getId())).toList();
        List<LocationEntity> locationsToDelete = locations.stream()
                .filter(location -> !userCommand.getLocationIds().contains(location.getId())).toList();

        // Get a list of group and location ids that need to be added
        List<Long> currentGroupIds = groups.stream().map(GroupEntity::getId).toList();
        List<Long> groupIdsToAdd = userCommand.getGroupIds().stream().filter(groupId -> !currentGroupIds.contains(groupId)).toList();

        List<Long> currentLocationIds = locations.stream().map(LocationEntity::getId).toList();
        List<Long> locationIdsToAdd = userCommand.getLocationIds().stream().filter(locationId -> !currentLocationIds.contains(locationId)).toList();

        // Remove old groups and locations
        groups.removeAll(groupsToDelete);
        locations.removeAll(locationsToDelete);

        // Add new groups to two lists. One that contains all users groups (for validation) and one that contains only the new ones that have to be added to the database
        List<GroupEntity> groupsToAdd = new ArrayList<>();
        for (long groupId : groupIdsToAdd) {
            GroupEntity group = groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
            groups.add(group);
            groupsToAdd.add(group);
        }

        // Add new locations to two lists. One that contains all users locations (for validation) and one that contains only the new ones that have to be added to the database
        List<LocationEntity> locationsToAdd = new ArrayList<>();
        for (long locationId : locationIdsToAdd) {
            LocationEntity locationEntity = locationRepository.findById(locationId).orElseThrow(() -> new LocationNotFoundException(locationId));
            locations.add(locationEntity);
            locationsToAdd.add(locationEntity);
        }

        // Check locations for correct countrycode and check if all groups are part of the locations
        this.validateLocationsAndGroups(userEntity.getCountry(), locations, groups);

        // Delete old groups from database
        groupsToDelete.forEach(group -> userToGroupRepository.deleteByUserIdAndGroupId(userId, group.getId()));
        locationsToDelete.forEach(location -> userToLocationRepository.deleteByUserIdAndLocationId(userId, location.getId()));

        // Add new groups and locations to database
        addGroupsAndLocationsToUser(userEntity, groupsToAdd, locationsToAdd);

        return userPersistenceMapper.toUser(userEntity);
    }

    private void addGroupsAndLocationsToUser(UserEntity userEntity, List<GroupEntity> groups, List<LocationEntity> locations) {
        for (GroupEntity groupEntity : groups) {
            UserToGroupEntity userToGroupEntity = new UserToGroupEntity(userEntity, groupEntity);
            this.userToGroupRepository.save(userToGroupEntity);
        }

        for (LocationEntity locationEntity : locations) {
            UserToLocationEntity userToLocationEntity = new UserToLocationEntity(userEntity, locationEntity);
            this.userToLocationRepository.save(userToLocationEntity);
        }
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
