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
import org.mapstruct.factory.Mappers;
import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.TemplateUser;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.TemplateUserCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.TemplateUserEntity;

@Mapper(componentModel = "spring", uses = { EcmrPersistenceMapper.class })
public interface TemplateUserPersistenceMapper {

    EcmrPersistenceMapper ecmrPersistenceMapper = Mappers.getMapper(EcmrPersistenceMapper.class);

    @Mapping(target = "templateUserNumber", ignore = true)
    TemplateUserEntity toEntity(TemplateUserCommand templateUserCommand, EcmrType type, EcmrStatus ecmrStatus);

    default EcmrEntity map(EcmrCommand value) {
        return this.ecmrPersistenceMapper.toEntity(value, EcmrType.TEMPLATE, EcmrStatus.NEW);
    }

    TemplateUser toTemplateUser(TemplateUserEntity templateUserEntity);
}


