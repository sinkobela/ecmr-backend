/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.mappers;

import org.mapstruct.Mapper;
import org.openlogisticsfoundation.ecmr.domain.models.User;
import org.openlogisticsfoundation.ecmr.domain.models.commands.UserCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserEntity;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
    UserEntity toUserEntity(UserCommand userCommand);

    User toUser(UserEntity entity);
}
