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
import org.openlogisticsfoundation.ecmr.domain.models.User;
import org.openlogisticsfoundation.ecmr.domain.models.commands.UserCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserEntity;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
    @Mapping(target = "id", ignore = true)
    UserEntity toUserEntity(UserCommand userCommand);

    @Mapping(target = "id", ignore = true)
    UserEntity toUserEntity(@MappingTarget UserEntity entity, UserCommand userCommand);

    User toUser(UserEntity entity);
}
