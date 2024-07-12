/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.mappers;

import org.mapstruct.Mapper;
import org.openlogisticsfoundation.ecmr.domain.models.TemplateUserModel;
import org.openlogisticsfoundation.ecmr.domain.models.commands.TemplateUserCommand;

@Mapper(componentModel = "spring", uses = EcmrWebMapper.class)
public interface TemplateUserWebMapper {
    TemplateUserCommand toCommand(TemplateUserModel templateUserModel);
}
