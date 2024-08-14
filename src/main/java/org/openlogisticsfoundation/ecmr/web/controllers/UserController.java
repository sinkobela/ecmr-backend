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
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
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
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserWebMapper userWebMapper;
    private final AuthenticationService authenticationService;

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> groups = this.userService.getAllUsers();
        return ResponseEntity.ok(groups);
    }

    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> createUser(@RequestBody @Valid UserCreationAndUpdateModel userCreationAndUpdateModel) throws AuthenticationException {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            UserCommand command = userWebMapper.toCommand(userCreationAndUpdateModel);
            User user = userService.createUser(authenticatedUser, command);
            return ResponseEntity.ok(user);
        } catch (ValidationException | GroupNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @PostMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateUser(@PathVariable long id, @RequestBody @Valid UserCreationAndUpdateModel userCreationAndUpdateModel) throws AuthenticationException {
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

    @GetMapping("/{id}/groups")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Group>> getGroupsForUser(@PathVariable long id) {
        return ResponseEntity.ok(userService.getGroupsByUserId(id));
    }
}
