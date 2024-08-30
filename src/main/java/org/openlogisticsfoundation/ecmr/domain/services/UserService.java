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
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.UserAlreadyExistsException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.UserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.mappers.GroupPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.mappers.UserPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.domain.models.User;
import org.openlogisticsfoundation.ecmr.domain.models.commands.UserCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.GroupEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserToGroupEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.GroupRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.UserRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.UserToGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPersistenceMapper userPersistenceMapper;
    private final GroupRepository groupRepository;
    private final UserToGroupRepository userToGroupRepository;
    private final GroupPersistenceMapper groupPersistenceMapper;
    private final GroupService groupService;

    public List<String> getAllUserEmails() {
        return userRepository.findAllByDeactivatedFalse().stream().map(UserEntity::getEmail).toList();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream().map(userPersistenceMapper::toUser).toList();
    }

    public User createUser(AuthenticatedUser authenticatedUser, @Valid UserCommand userCommand)
            throws ValidationException, GroupNotFoundException, NoPermissionException, UserAlreadyExistsException {
        UserEntity userEntity = userPersistenceMapper.toUserEntity(userCommand);
        List<GroupEntity> groups = new ArrayList<>();
        for (long groupId : userCommand.getGroupIds()) {
            groups.add(groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId)));
        }

        this.validateAndSetGroup(authenticatedUser, userCommand, userEntity);

        try{
            userEntity = userRepository.save(userEntity);
        } catch (Exception e) {
            throw new UserAlreadyExistsException(userEntity.getEmail());
        }

        for (GroupEntity groupEntity : groups) {
            UserToGroupEntity userToGroupEntity = new UserToGroupEntity(userEntity, groupEntity);
            this.userToGroupRepository.save(userToGroupEntity);
        }
        return userPersistenceMapper.toUser(userEntity);
    }

    private void validateAndSetGroup(AuthenticatedUser authenticatedUser, @Valid UserCommand userCommand, UserEntity userEntity)
            throws NoPermissionException, ValidationException, GroupNotFoundException {
        if (!groupService.areAllGroupIdsPartOfUsersGroup(authenticatedUser, userCommand.getGroupIds())) {
            throw new NoPermissionException("At least one group without permission");
        }

        if (userCommand.getGroupIds().isEmpty()) {
            userEntity.setDefaultGroup(null);
        } else {
            if (!userCommand.getGroupIds().contains(userCommand.getDefaultGroupId())) {
                throw new ValidationException("Default group has to be contained in group Ids");
            }
            GroupEntity defaultGroup = groupRepository.findById(userCommand.getDefaultGroupId()).orElseThrow(() -> new GroupNotFoundException(userCommand.getDefaultGroupId()));
            userEntity.setDefaultGroup(defaultGroup);
        }
    }

    public List<Group> getGroupsByUserId(Long userId) {
        return userToGroupRepository.findGroupsByUserId(userId).stream().map(groupPersistenceMapper::toGroup).toList();
    }

    @Transactional
    public User updateUser(AuthenticatedUser authenticatedUser, long userId, UserCommand userCommand) throws UserNotFoundException, GroupNotFoundException, ValidationException, NoPermissionException {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        userEntity = userPersistenceMapper.toUserEntity(userEntity, userCommand);

        this.validateAndSetGroup(authenticatedUser, userCommand, userEntity);

        // Get the current groups of the user
        List<GroupEntity> groups = userToGroupRepository.findGroupsByUserId(userId);

        // Get a list of groups that need to be deleted
        List<GroupEntity> groupsToDelete = groups.stream()
                .filter(group -> !userCommand.getGroupIds().contains(group.getId())).toList();

        // Get a list of groups ids that need to be added
        List<Long> currentGroupIds = groups.stream().map(GroupEntity::getId).toList();
        List<Long> groupIdsToAdd = userCommand.getGroupIds().stream().filter(groupId -> !currentGroupIds.contains(groupId)).toList();

        // Remove old groups
        groups.removeAll(groupsToDelete);

        // Add new groups to two lists. One that contains all users groups (for validation) and one that contains only the new ones that have to be added to the database
        List<GroupEntity> groupsToAdd = new ArrayList<>();
        for (long groupId : groupIdsToAdd) {
            GroupEntity group = groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
            groups.add(group);
            groupsToAdd.add(group);
        }

        if (userEntity.getId() == authenticatedUser.getUser().getId() && (!groupsToAdd.isEmpty() || !groupsToDelete.isEmpty())) {
            throw new NoPermissionException("A user can't change his own groups");
        }

        userEntity = userRepository.save(userEntity);

        // Delete old groups from database
        groupsToDelete.forEach(group -> userToGroupRepository.deleteByUserIdAndGroupId(userId, group.getId()));

        // Add new groups to database
        for (GroupEntity groupEntity : groupsToAdd) {
            UserToGroupEntity userToGroupEntity = new UserToGroupEntity(userEntity, groupEntity);
            this.userToGroupRepository.save(userToGroupEntity);
        }

        return userPersistenceMapper.toUser(userEntity);
    }

    public List<User> getUsersByGroupId(long groupId) {
        return userToGroupRepository.findUsersByGroupId(groupId).stream().map(userPersistenceMapper::toUser).toList();
    }

    public User getActiveUserByEmail(String email) throws UserNotFoundException {
        UserEntity userEntity = userRepository.findByEmailAndDeactivatedFalse(email).orElseThrow(() -> new UserNotFoundException(email));
        return userPersistenceMapper.toUser(userEntity);
    }

    public User getActiveUserById(Long userId) throws UserNotFoundException {
        UserEntity userEntity = userRepository.findByIdAndDeactivatedFalse(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return userPersistenceMapper.toUser(userEntity);
    }

    public void changeUserActiveState(AuthenticatedUser authenticatedUser, long userId, boolean isDeactivated) throws NoPermissionException,
            UserNotFoundException {
        if(authenticatedUser.getUser().getId() == userId) {
            throw new NoPermissionException("A user can't activate itself");
        }
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        userEntity.setDeactivated(isDeactivated);
        userRepository.save(userEntity);
    }
}
