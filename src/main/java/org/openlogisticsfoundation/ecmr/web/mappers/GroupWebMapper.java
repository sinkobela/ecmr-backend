/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.mappers;

import org.mapstruct.Mapper;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.domain.models.commands.GroupCreationCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.GroupUpdateCommand;
import org.openlogisticsfoundation.ecmr.web.models.GroupCreationModel;
import org.openlogisticsfoundation.ecmr.web.models.GroupFlatModel;
import org.openlogisticsfoundation.ecmr.web.models.GroupUpdateModel;

@Mapper(componentModel = "spring")
public interface GroupWebMapper {
    GroupCreationCommand toCommand(GroupCreationModel model);

    GroupUpdateCommand toCommand(GroupUpdateModel model);

    GroupFlatModel toFlatModel(Group group);
}
