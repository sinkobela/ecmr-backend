/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import java.util.List;

import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.domain.models.Location;
import org.openlogisticsfoundation.ecmr.domain.models.User;
import org.openlogisticsfoundation.ecmr.domain.models.commands.UserCommand;
import org.openlogisticsfoundation.ecmr.domain.services.UserService;
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
    public ResponseEntity<User> createUser(@RequestBody @Valid UserCreationAndUpdateModel userCreationAndUpdateModel) {
        try {
            UserCommand command = userWebMapper.toCommand(userCreationAndUpdateModel);
            User user = userService.createUser(command);
            return ResponseEntity.ok(user);
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}/groups")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Group>> getGroupsForUser(@PathVariable long id) {
        return ResponseEntity.ok(userService.getGroupsByUserId(id));
    }

    @GetMapping("/{id}/locations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Location>> getLocationsForUser(@PathVariable long id) {
        return ResponseEntity.ok(userService.getLocationsByUserId(id));
    }
}
