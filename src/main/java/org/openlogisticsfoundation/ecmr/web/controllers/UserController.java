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
import org.openlogisticsfoundation.ecmr.domain.exceptions.GroupNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.UserAlreadyExistsException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.UserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.domain.models.User;
import org.openlogisticsfoundation.ecmr.domain.models.commands.UserCommand;
import org.openlogisticsfoundation.ecmr.domain.services.UserService;
import org.openlogisticsfoundation.ecmr.web.exceptions.AuthenticationException;
import org.openlogisticsfoundation.ecmr.web.mappers.UserWebMapper;
import org.openlogisticsfoundation.ecmr.web.models.UserCreationAndUpdateModel;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserWebMapper userWebMapper;
    private final AuthenticationService authenticationService;

    /**
     * Retrieves the currently authenticated user.
     *
     * @return The authenticated user.
     */
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "User",
        summary = "Get Current User",
        responses = {
            @ApiResponse(description = "Authenticated user",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthenticatedUser.class))),
            @ApiResponse(description = "Unauthorized access", responseCode = "401")
        })
    public ResponseEntity<AuthenticatedUser> current() {
        try {
            return ResponseEntity.ok(authenticationService.getAuthenticatedUser());
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }


    /**
     * Retrieves all user emails.
     *
     * @return List of user emails.
     */
    @GetMapping("/mail")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "User",
        summary = "Get All User Emails",
        responses = {
            @ApiResponse(description = "List of user emails",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "array", implementation = String.class)))
        })
    public ResponseEntity<List<String>> getAllUserMails() {
        List<String> groups = this.userService.getAllUserEmails();
        return ResponseEntity.ok(groups);
    }

    /**
     * Retrieves all users.
     *
     * @return List of users.
     */
    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "User",
        summary = "Get All Users",
        responses = {
            @ApiResponse(description = "List of users",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = User.class)))
        })
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> groups = this.userService.getAllUsers();
        return ResponseEntity.ok(groups);
    }

    /**
     * Creates a new user.
     *
     * @param userCreationAndUpdateModel The user details for creation.
     * @return The created user.
     */
    @PostMapping()
    @PreAuthorize("isAuthenticated() && hasRole('Admin')")
    @Operation(
        tags = "User",
        summary = "Create User",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = UserCreationAndUpdateModel.class))),
        responses = {
            @ApiResponse(description = "The created user",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = User.class))),
            @ApiResponse(description = "Bad request due to validation", responseCode = "400"),
            @ApiResponse(description = "User already exists", responseCode = "409"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<User> createUser(@RequestBody @Valid UserCreationAndUpdateModel userCreationAndUpdateModel) throws AuthenticationException {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser(true);
            UserCommand command = userWebMapper.toCommand(userCreationAndUpdateModel);
            User user = userService.createUser(authenticatedUser, command);
            return ResponseEntity.ok(user);
        } catch (ValidationException | GroupNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (UserAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /**
     * Updates an existing user.
     *
     * @param id                         The ID of the user to update.
     * @param userCreationAndUpdateModel The updated user details.
     * @return The updated user.
     */
    @PostMapping("/{id}")
    @PreAuthorize("isAuthenticated() && hasRole('Admin')")
    @Operation(
        tags = "User",
        summary = "Update User",
        parameters = {
            @Parameter(name = "id", description = "ID of the user to update", required = true, schema = @Schema(type = "integer"))
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = UserCreationAndUpdateModel.class))),
        responses = {
            @ApiResponse(description = "The updated user",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = User.class))),
            @ApiResponse(description = "User not found", responseCode = "404"),
            @ApiResponse(description = "Bad request due to validation", responseCode = "400"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<User> updateUser(@PathVariable long id, @RequestBody @Valid UserCreationAndUpdateModel userCreationAndUpdateModel)
            throws AuthenticationException {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            UserCommand command = userWebMapper.toCommand(userCreationAndUpdateModel);
            User user = userService.updateUser(authenticatedUser, id, command);
            return ResponseEntity.ok(user);
        } catch (ValidationException | GroupNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Retrieves groups for a specific user.
     *
     * @param id The ID of the user.
     * @return List of groups for the user.
     */
    @GetMapping("/{id}/groups")
    @PreAuthorize("isAuthenticated() && hasRole('Admin')")
    @Operation(
        tags = "User",
        summary = "Get Groups for User",
        parameters = {
            @Parameter(name = "id", description = "ID of the user", required = true, schema = @Schema(type = "integer"))
        },
        responses = {
            @ApiResponse(description = "List of groups for the user",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Group.class))),
            @ApiResponse(description = "User not found", responseCode = "404")
        })
    public ResponseEntity<List<Group>> getGroupsForUser(@PathVariable long id) {
        return ResponseEntity.ok(userService.getGroupsByUserId(id));
    }

    /**
     * Activates a user by ID.
     *
     * @param id The ID of the user to activate.
     * @return No content.
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("isAuthenticated() && hasRole('Admin')")
    @Operation(
        tags = "User",
        summary = "Activate User",
        parameters = {
            @Parameter(name = "id", description = "ID of the user to activate", required = true, schema = @Schema(type = "integer"))
        },
        responses = {
            @ApiResponse(description = "User activated successfully", responseCode = "200"),
            @ApiResponse(description = "User not found", responseCode = "404"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<Void> activateUser(@PathVariable long id)
            throws AuthenticationException {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            userService.changeUserActiveState(authenticatedUser, id, false);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Deactivates a user by ID.
     *
     * @param id The ID of the user to deactivate.
     * @return No content.
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("isAuthenticated() && hasRole('Admin')")
    @Operation(
        tags = "User",
        summary = "Deactivate User",
        parameters = {
            @Parameter(name = "id", description = "ID of the user to deactivate", required = true, schema = @Schema(type = "integer"))
        },
        responses = {
            @ApiResponse(description = "User deactivated successfully", responseCode = "200"),
            @ApiResponse(description = "User not found", responseCode = "404"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<Void> deactivateUser(@PathVariable long id)
            throws AuthenticationException {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            userService.changeUserActiveState(authenticatedUser, id, true);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

}
