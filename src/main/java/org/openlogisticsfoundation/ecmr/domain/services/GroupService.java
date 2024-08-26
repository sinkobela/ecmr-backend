/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.openlogisticsfoundation.ecmr.domain.exceptions.GroupHasChildrenException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.GroupHasNoParentException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.GroupNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.mappers.GroupPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.domain.models.User;
import org.openlogisticsfoundation.ecmr.domain.models.commands.GroupCreationCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.GroupUpdateCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrAssignmentEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.GroupEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserToGroupEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrAssignmentRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.GroupRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.UserRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.UserToGroupRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupPersistenceMapper groupPersistenceMapper;
    private final UserToGroupRepository userToGroupRepository;
    private final UserRepository userRepository;
    private final EcmrAssignmentRepository ecmrAssignmentRepository;

    public List<Group> getAllGroups() {
        List<GroupEntity> groupEntities = groupRepository.findByParentId(null);
        return groupEntities.stream().map(groupPersistenceMapper::toGroup).toList();
    }

    public List<Group> getGroupsForUser(AuthenticatedUser authenticatedUser) {
       return this.getGroupsForUser(authenticatedUser.getUser().getId());
    }

    public List<Group> getGroupsForUser(long userId) {
        List<GroupEntity> usersGroupEntities = userToGroupRepository.findGroupsByUserId(userId);
        List<Group> usersGroups = usersGroupEntities.stream().map(groupPersistenceMapper::toGroup).toList();
        usersGroups = removeGroupsThatAreDescendantsOfOtherGroups(usersGroups);
        return usersGroups;
    }

    public boolean areAllGroupIdsPartOfUsersGroup(AuthenticatedUser authenticatedUser, List<Long> groupIds) {
        List<Group> usersGroups = getGroupsForUser(authenticatedUser);
        List<Long> usersGroupIds = flatMapGroupTrees(usersGroups).stream().map(Group::getId).toList();
        return groupIds.stream().allMatch(usersGroupIds::contains);
    }

    private List<Group> removeGroupsThatAreDescendantsOfOtherGroups(List<Group> groups) {
        List<Group> groupWithoutDuplicateDescendants = new ArrayList<>();

        // Assignment from group to a list that contains this group and all of its descendants
        Map<Group, List<Group>> groupToFlattenedGroup = new HashMap<>();
        for (Group group : groups) {
            groupToFlattenedGroup.put(group, flatMapGroupTree(group).toList());
        }

        for (Group group : groups) {
            if (groupToFlattenedGroup.entrySet().stream().noneMatch(kv -> kv.getKey() != group && kv.getValue().contains(group))) {
                groupWithoutDuplicateDescendants.add(group);
            }
        }

        return groupWithoutDuplicateDescendants;
    }

    public Group getGroup(long id) throws GroupNotFoundException {
        return groupRepository.findById(id).map(groupPersistenceMapper::toGroup).orElseThrow(() -> new GroupNotFoundException(id));
    }

    public Group createGroup(AuthenticatedUser authenticatedUser, @Valid GroupCreationCommand command) throws GroupNotFoundException, NoPermissionException {
        GroupEntity parentGroup = groupRepository.findById(command.getParentId())
                .orElseThrow(() -> new GroupNotFoundException(command.getParentId()));
        if (!areAllGroupIdsPartOfUsersGroup(authenticatedUser, List.of(command.getParentId()))) {
            throw new NoPermissionException("No permission for parent group id " + command.getParentId());
        }

        GroupEntity group = new GroupEntity();
        group.setName(command.getName().trim());
        group.setDescription(command.getDescription());
        group.setParent(parentGroup);
        group.setChildren(new ArrayList<>());
        group = groupRepository.save(group);
        return getGroup(group.getId());
    }

    public Group updateGroup(long id, @Valid GroupUpdateCommand command) throws GroupNotFoundException {
        GroupEntity groupEntity = groupRepository.findById(id).orElseThrow(() -> new GroupNotFoundException(id));
        groupEntity.setName(command.getName());
        groupEntity.setDescription(command.getDescription());
        groupEntity = groupRepository.save(groupEntity);
        return getGroup(groupEntity.getId());
    }

    @Transactional
    public Boolean deleteGroup(long id) throws GroupNotFoundException, GroupHasChildrenException, GroupHasNoParentException {
        GroupEntity groupEntity = groupRepository.findById(id).orElseThrow(() -> new GroupNotFoundException(id));
        if(groupEntity.getChildren().isEmpty() && groupEntity.getParent() != null) {
            GroupEntity parentEntity = groupEntity.getParent();

            //Update user group id & user default group
            List<UserEntity> userList = userToGroupRepository.findUsersByGroupId(id);
            updateUsersGroup(userList, parentEntity, groupEntity);

            // Update ECMR Assignments to the parent group
            List<EcmrAssignmentEntity> ecmrAssignments = ecmrAssignmentRepository.findByGroup_Id(id);
            updateEcmrAssignmentsGroup(ecmrAssignments, parentEntity);

            groupRepository.delete(groupEntity);
            return true;
        } else if(!groupEntity.getChildren().isEmpty()){
            throw new GroupHasChildrenException(groupEntity.getId());
        } else {
            throw new GroupHasNoParentException(groupEntity.getId());
        }
    }

    public void updateUsersGroup(List<UserEntity> users, GroupEntity  newGroup, GroupEntity oldGroup) {
        for (UserEntity user : users) {
            userToGroupRepository.deleteByUserIdAndGroupId(user.getId(), oldGroup.getId());

            UserToGroupEntity newUserToGroup = new UserToGroupEntity(user, newGroup);
            userToGroupRepository.save(newUserToGroup);

            if(user.getDefaultGroup().getId() == oldGroup.getId()){
                user.setDefaultGroup(newGroup);
                userRepository.save(user);
            }
        }
    }

    public void updateEcmrAssignmentsGroup(List<EcmrAssignmentEntity> ecmrAssignments, GroupEntity newGroup) {
        for (EcmrAssignmentEntity assignment : ecmrAssignments) {
            assignment.setGroup(newGroup);
            ecmrAssignmentRepository.save(assignment);
        }
    }

    public Group updateGroupParent(long groupId, long groupParentId) throws GroupNotFoundException, ValidationException {

        Group group = getGroup(groupId);
        List<Group> groupDescendants = flatMapGroupTree(group).toList();
        // Check if the groupParentId is contained in the list of descendants of the group
        if (groupDescendants.stream().map(Group::getId).toList().contains(groupParentId)) {
            throw new ValidationException("Can't set parent to a group that is a descendant of the group");
        }
        GroupEntity groupEntity = groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
        GroupEntity parentGroup = groupRepository.findById(groupParentId).orElseThrow(() -> new GroupNotFoundException(groupParentId));
        groupEntity.setParent(parentGroup);
        groupEntity = groupRepository.save(groupEntity);
        return getGroup(groupEntity.getId());
    }

    private Stream<Group> flatMapGroupTree(Group group) {
        return Stream.concat(Stream.of(group), group.getChildren().stream().flatMap(this::flatMapGroupTree));
    }

    public List<Group> flatMapGroupTrees(List<Group> group) {
        return group.stream().flatMap(this::flatMapGroupTree).distinct().toList();
    }

    List<GroupEntity> getGroupEntities(List<Long> groupIds) {
        return groupRepository.findAllById(groupIds);
    }
}
