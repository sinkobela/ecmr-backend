/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import java.util.List;

import org.openlogisticsfoundation.ecmr.domain.exceptions.GroupNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.LocationNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.domain.models.User;
import org.openlogisticsfoundation.ecmr.domain.models.commands.GroupCommand;
import org.openlogisticsfoundation.ecmr.domain.services.GroupService;
import org.openlogisticsfoundation.ecmr.domain.services.UserService;
import org.openlogisticsfoundation.ecmr.web.mappers.GroupWebMapper;
import org.openlogisticsfoundation.ecmr.web.models.GroupCreationAndUpdateModel;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final GroupWebMapper groupWebMapper;

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Group>> getAllGroups() {
        List<Group> groups = this.groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Group> getGroup(@PathVariable long id) {
        try {
            return ResponseEntity.ok(groupService.getGroup(id));
        } catch (GroupNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Group> createGroup(@RequestBody @Valid GroupCreationAndUpdateModel groupCreationAndUpdateModel) {
        GroupCommand groupCommand = groupWebMapper.toCommand(groupCreationAndUpdateModel);
        try {
            Group group = this.groupService.createGroup(groupCommand);
            return ResponseEntity.ok(group);
        } catch (LocationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found");
        }
    }

    @PostMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Group> updateGroup(@PathVariable long id, @RequestBody @Valid GroupCreationAndUpdateModel groupCreationAndUpdateModel) {
        try {
            GroupCommand command = groupWebMapper.toCommand(groupCreationAndUpdateModel);
            Group group = groupService.updateGroup(id, command);
            return ResponseEntity.ok(group);
        } catch (GroupNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<User>> getUsersForGroup(@PathVariable long id) {
        return ResponseEntity.ok(userService.getUsersByGroupId(id));
    }
}
