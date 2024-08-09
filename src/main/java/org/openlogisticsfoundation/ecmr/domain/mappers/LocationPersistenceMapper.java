/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.openlogisticsfoundation.ecmr.domain.models.Location;
import org.openlogisticsfoundation.ecmr.domain.models.commands.LocationCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.LocationEntity;

@Mapper(componentModel = "spring")
public interface LocationPersistenceMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "groups", ignore = true)
    LocationEntity toLocationEntity(LocationCommand locationCommand);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "groups", ignore = true)
    LocationEntity toLocationEntity(@MappingTarget LocationEntity locationEntity, LocationCommand locationCommand);

    Location toLocation(LocationEntity locationEntity);
}
