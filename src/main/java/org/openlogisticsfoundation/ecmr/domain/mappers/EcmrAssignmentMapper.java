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
import org.openlogisticsfoundation.ecmr.domain.models.EcmrAssignment;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrAssignmentEntity;

@Mapper(componentModel = "spring", uses = {GroupPersistenceMapper.class, ExternalUserPersistenceMapper.class})
public interface EcmrAssignmentMapper {
    @Mapping(target = "ecmrId", source = "ecmr.ecmrId")
    EcmrAssignment map(EcmrAssignmentEntity ecmrAssignment);
}
