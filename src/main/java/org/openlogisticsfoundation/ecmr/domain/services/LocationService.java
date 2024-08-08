/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.List;

import org.openlogisticsfoundation.ecmr.domain.mappers.LocationPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.Location;
import org.openlogisticsfoundation.ecmr.domain.models.commands.LocationCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.LocationEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.LocationRepository;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationPersistenceMapper locationPersistenceMapper;

    public List<Location> getAllLocations() {
        return locationRepository.findAll().stream().map(locationPersistenceMapper::toLocation).toList();
    }

    public Location createLocation(@Valid LocationCommand locationCommand) {
        LocationEntity location = locationPersistenceMapper.toLocationEntity(locationCommand);
        return locationPersistenceMapper.toLocation(locationRepository.save(location));
    }
}
