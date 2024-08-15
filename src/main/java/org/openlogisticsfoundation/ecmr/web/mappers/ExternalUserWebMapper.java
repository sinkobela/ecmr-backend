/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.mappers;

import org.mapstruct.Mapper;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ExternalUserRegistrationCommand;
import org.openlogisticsfoundation.ecmr.web.models.ExternalUserRegistrationModel;

@Mapper(componentModel = "spring")
public interface ExternalUserWebMapper {
    ExternalUserRegistrationCommand map(ExternalUserRegistrationModel model);
}
