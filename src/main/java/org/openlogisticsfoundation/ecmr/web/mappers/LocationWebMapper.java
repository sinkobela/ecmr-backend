/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.openlogisticsfoundation.ecmr.domain.models.commands.LocationCommand;
import org.openlogisticsfoundation.ecmr.web.models.LocationCreationAndUpdateModel;
import org.springframework.validation.Validator;

@Mapper(componentModel = "spring")
public interface LocationWebMapper {
    LocationCommand toCommand(LocationCreationAndUpdateModel locationCreationAndUpdateModel);
}
