/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.openlogisticsfoundation.ecmr.domain.exceptions.*;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.domain.models.User;
import org.openlogisticsfoundation.ecmr.domain.models.commands.GroupCreationCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.GroupUpdateCommand;
import org.openlogisticsfoundation.ecmr.domain.services.GroupService;
import org.openlogisticsfoundation.ecmr.domain.services.UserService;
import org.openlogisticsfoundation.ecmr.web.exceptions.AuthenticationException;
import org.openlogisticsfoundation.ecmr.web.mappers.GroupWebMapper;
import org.openlogisticsfoundation.ecmr.web.models.GroupCreationModel;
import org.openlogisticsfoundation.ecmr.web.models.GroupFlatModel;
import org.openlogisticsfoundation.ecmr.web.models.GroupParentUpdateModel;
import org.openlogisticsfoundation.ecmr.web.models.GroupUpdateModel;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final GroupWebMapper groupWebMapper;

    /**
     * Retrieves all groups for the authenticated user or all groups if specified
     *
     * @param currentUserGroupsOnly If true, only groups for the current user are retrieved
     * @return A list of groups
     */
    @GetMapping()
    @PreAuthorize("isAuthenticated() && hasRole('Admin')")
    @Operation(
        tags = "Group",
        summary = "Retrieve All Groups",
        parameters = {
            @Parameter(name = "currentUserGroupsOnly", description = "Retrieve only current user's groups", required = false, schema = @Schema(type = "boolean"))
        },
        responses = {
            @ApiResponse(description = "List of groups",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Group.class))),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<List<Group>> getAllGroups(@RequestParam(defaultValue = "false") boolean currentUserGroupsOnly) throws AuthenticationException {
        List<Group> groups;
        AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser(true);
        if (currentUserGroupsOnly) {
            groups = this.groupService.getGroupsForUser(authenticatedUser);
        } else {
            groups = this.groupService.getAllGroups();
        }
        return ResponseEntity.ok(groups);
    }

    /**
     * Retrieves all groups as a flat list for the authenticated user or all groups if specified
     *
     * @param currentUserGroupsOnly If true, only groups for the current user are retrieved
     * @return A flat list of groups
     */
    @GetMapping("/flat-list")
    @PreAuthorize("isAuthenticated() && hasRole('Admin')")
    @Operation(
        tags = "Group",
        summary = "Retrieve All Groups as Flat List",
        parameters = {
            @Parameter(name = "currentUserGroupsOnly", description = "Retrieve only current user's groups", required = false, schema = @Schema(type = "boolean"))
        },
        responses = {
            @ApiResponse(description = "Flat list of groups",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GroupFlatModel.class))),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<List<GroupFlatModel>> getAllGroupsAsFlatList(@RequestParam(defaultValue = "false") boolean currentUserGroupsOnly) throws AuthenticationException {
        List<Group> groups;
        if (currentUserGroupsOnly) {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            groups = this.groupService.getGroupsForUser(authenticatedUser);
        } else {
            groups = this.groupService.getAllGroups();
        }
        groups = groupService.flatMapGroupTrees(groups);
        return ResponseEntity.ok(groups.stream().map(groupWebMapper::toFlatModel).toList());
    }

    /**
     * Retrieves a specific group by ID
     *
     * @param id The ID of the group
     * @return The requested group
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated() && hasRole('Admin')")
    @Operation(
        tags = "Group",
        summary = "Retrieve Group by ID",
        parameters = {
            @Parameter(name = "id", description = "UUID of the group", required = true, schema = @Schema(type = "integer"))
        },
        responses = {
            @ApiResponse(description = "The requested group",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Group.class))),
            @ApiResponse(description = "Group not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<Group> getGroup(@PathVariable long id) {
        try {
            return ResponseEntity.ok(groupService.getGroup(id));
        } catch (GroupNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Creates a new group
     *
     * @param groupCreationModel The details of the group to create
     * @return The created group
     */
    @PostMapping()
    @PreAuthorize("isAuthenticated() && hasRole('Admin')")
    @Operation(
        tags = "Group",
        summary = "Create a New Group",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = GroupCreationModel.class))),
        responses = {
            @ApiResponse(description = "The created group",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Group.class))),
            @ApiResponse(description = "Group not found", responseCode = "404"),
            @ApiResponse(description = "Forbidden access", responseCode = "403"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401")
        })
    public ResponseEntity<Group> createGroup(@RequestBody @Valid GroupCreationModel groupCreationModel) throws AuthenticationException {
        GroupCreationCommand groupCreationCommand = groupWebMapper.toCommand(groupCreationModel);
        AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
        try {
            Group group = this.groupService.createGroup(authenticatedUser, groupCreationCommand);
            return ResponseEntity.ok(group);
        } catch (GroupNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Updates an existing group
     *
     * @param id The ID of the group to update
     * @param groupUpdateModel The updated details of the group
     * @return The updated group
     */
    @PostMapping("/{id}")
    @PreAuthorize("isAuthenticated() && hasRole('Admin')")
    @Operation(
        tags = "Group",
        summary = "Update Existing Group",
        parameters = {
            @Parameter(name = "id", description = "UUID of the group to update", required = true, schema = @Schema(type = "integer"))
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = GroupUpdateModel.class))),
        responses = {
            @ApiResponse(description = "The updated group",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Group.class))),
            @ApiResponse(description = "Group not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<Group> updateGroup(@PathVariable long id, @RequestBody @Valid GroupUpdateModel groupUpdateModel) {
        try {
            GroupUpdateCommand command = groupWebMapper.toCommand(groupUpdateModel);
            Group group = groupService.updateGroup(id, command);
            return ResponseEntity.ok(group);
        } catch (GroupNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Deletes a group by ID
     *
     * @param id The ID of the group to delete
     * @return True if the group was deleted successfully
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated() && hasRole('Admin')")
    @Operation(
        tags = "Group",
        summary = "Delete Group",
        parameters = {
            @Parameter(name = "id", description = "UUID of the group to delete", required = true, schema = @Schema(type = "integer"))
        },
        responses = {
            @ApiResponse(description = "Group deleted successfully", responseCode = "204"),
            @ApiResponse(description = "Group not found", responseCode = "404"),
            @ApiResponse(description = "Bad request due to group constraints", responseCode = "400"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401")
        })
    public ResponseEntity<Boolean> deleteGroup(@PathVariable long id) {
        try {
            Boolean deleteResult = groupService.deleteGroup(id);
            return ResponseEntity.ok(deleteResult);
        } catch (GroupNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (GroupHasChildrenException | GroupHasNoParentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Updates the parent of a group
     *
     * @param id The ID of the group to update
     * @param groupParentUpdateModel The new parent group details
     * @return The updated group
     */
    @PostMapping("/{id}/update-parent")
    @PreAuthorize("isAuthenticated() && hasRole('Admin')")
    @Operation(
        tags = "Group",
        summary = "Update Group Parent",
        parameters = {
            @Parameter(name = "id", description = "UUID of the group to update", required = true, schema = @Schema(type = "integer"))
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = GroupParentUpdateModel.class))),
        responses = {
            @ApiResponse(description = "The updated group",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Group.class))),
            @ApiResponse(description = "Group not found", responseCode = "404"),
            @ApiResponse(description = "Bad request due to validation", responseCode = "400"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<Group> updateGroupParent(@PathVariable long id, @RequestBody @Valid GroupParentUpdateModel groupParentUpdateModel) {
        try {
            Group group = groupService.updateGroupParent(id, groupParentUpdateModel.getParentId());
            return ResponseEntity.ok(group);
        } catch (GroupNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Retrieves users belonging to a specific group
     *
     * @param id The ID of the group
     * @return A list of users in the group
     */
    @GetMapping("/{id}/users")
    @PreAuthorize("isAuthenticated() && hasRole('Admin')")
    @Operation(
        tags = "Group",
        summary = "Get Users for Group",
        parameters = {
            @Parameter(name = "id", description = "UUID of the group", required = true, schema = @Schema(type = "integer"))
        },
        responses = {
            @ApiResponse(description = "List of users in the group",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = User.class))),
            @ApiResponse(description = "Group not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401")
        })
    public ResponseEntity<List<User>> getUsersForGroup(@PathVariable long id) {
        return ResponseEntity.ok(userService.getUsersByGroupId(id));
    }
}
