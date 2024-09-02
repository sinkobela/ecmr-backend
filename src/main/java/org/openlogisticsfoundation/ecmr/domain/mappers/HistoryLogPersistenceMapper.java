/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.mappers;

import org.mapstruct.Mapper;
import org.openlogisticsfoundation.ecmr.domain.models.HistoryLog;
import org.openlogisticsfoundation.ecmr.persistence.entities.HistoryLogEntity;

@Mapper(componentModel = "spring")
public interface HistoryLogPersistenceMapper {
    HistoryLog toModel(HistoryLogEntity historyLogEntity);
}
