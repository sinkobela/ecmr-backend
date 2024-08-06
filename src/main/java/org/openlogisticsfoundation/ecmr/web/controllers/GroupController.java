/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.openlogisticsfoundation.ecmr.domain.models.GroupModel;
import org.openlogisticsfoundation.ecmr.domain.services.GroupService;
import org.openlogisticsfoundation.ecmr.persistence.entities.LocationEntity;
import org.openlogisticsfoundation.ecmr.web.mappers.GroupWebMapper;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final AuthenticationService authenticationService;
    private final GroupWebMapper groupWebMapper;

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GroupModel>> getAllGroups() {
        List<GroupModel> groups = this.groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping(("/current-user"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GroupModel>> getAllGroupsForCurrentUser() {
        //TODO: Get locations of AuthenticatedUser
        ArrayList<LocationEntity> locations = new ArrayList<>(List.of(new LocationEntity()));
        List<GroupModel> groups = this.groupService.getAllGroupsForCurrentUser(locations);
        return ResponseEntity.ok(groups);
    }
}
