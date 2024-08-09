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
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.domain.models.Location;
import org.openlogisticsfoundation.ecmr.domain.models.User;
import org.openlogisticsfoundation.ecmr.domain.models.commands.LocationCommand;
import org.openlogisticsfoundation.ecmr.domain.services.GroupService;
import org.openlogisticsfoundation.ecmr.domain.services.LocationService;
import org.openlogisticsfoundation.ecmr.domain.services.UserService;
import org.openlogisticsfoundation.ecmr.web.mappers.LocationWebMapper;
import org.openlogisticsfoundation.ecmr.web.models.LocationCreationAndUpdateModel;
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
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;
    private final GroupService groupService;
    private final AuthenticationService authenticationService;
    private final LocationWebMapper locationWebMapper;
    private final UserService userService;

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Location>> getLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Location> getLocation(@PathVariable long id) {
        try {
            return ResponseEntity.ok(locationService.getLocation(id));
        } catch (LocationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{id}/groups")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Group>> getGroupsForLocation(@PathVariable long id) {
        return ResponseEntity.ok(groupService.getGroupsByLocationId(id));
    }

    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Location> createLocation(@RequestBody @Valid LocationCreationAndUpdateModel locationCreationAndUpdateModel) {
        LocationCommand command = locationWebMapper.toCommand(locationCreationAndUpdateModel);
        Location location = locationService.createLocation(command);
        return ResponseEntity.ok(location);
    }

    @PostMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Location> updateLocation(@PathVariable long id, @RequestBody @Valid LocationCreationAndUpdateModel locationCreationAndUpdateModel) {
        try {
            LocationCommand command = locationWebMapper.toCommand(locationCreationAndUpdateModel);
            Location location = locationService.updateLocation(id, command);
            return ResponseEntity.ok(location);
        } catch (LocationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<User>> getUsersForLocation(@PathVariable long id) {
        return ResponseEntity.ok(userService.getUsersByLocationId(id));
    }
}
