/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.mappers;

import org.mapstruct.Mapper;
import org.openlogisticsfoundation.ecmr.domain.models.GroupModel;
import org.openlogisticsfoundation.ecmr.domain.models.commands.GroupCommand;

@Mapper(componentModel = "spring")
public interface GroupWebMapper {
    GroupCommand toCommand(GroupModel groupModel);
}
