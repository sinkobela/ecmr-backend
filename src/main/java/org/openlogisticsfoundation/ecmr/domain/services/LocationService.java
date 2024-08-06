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
import org.openlogisticsfoundation.ecmr.domain.models.LocationModel;
import org.openlogisticsfoundation.ecmr.domain.models.commands.LocationCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.LocationEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.LocationRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationPersistenceMapper locationPersistenceMapper;

    public List<LocationModel> getAllLocations() {
        return locationRepository.findAll().stream().map(locationPersistenceMapper::toModel).toList();
    }

    public LocationModel createLocation(LocationCommand locationCommand) {
        LocationEntity location = locationPersistenceMapper.toEntity(locationCommand);
        return locationPersistenceMapper.toModel(locationRepository.save(location));
    }
}
