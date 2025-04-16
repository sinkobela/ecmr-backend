/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.models;

import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EcmrShareWithGroupModel {
    @NotNull
    private Long groupId;
    @NotNull
    private EcmrRole role;
}
