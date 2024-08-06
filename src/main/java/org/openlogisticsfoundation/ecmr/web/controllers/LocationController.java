/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import java.util.List;

import org.openlogisticsfoundation.ecmr.domain.models.LocationModel;
import org.openlogisticsfoundation.ecmr.domain.services.LocationService;
import org.openlogisticsfoundation.ecmr.web.mappers.LocationWebMapper;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;
    private final AuthenticationService authenticationService;
    private final LocationWebMapper locationWebMapper;

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LocationModel>> getLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }
}
