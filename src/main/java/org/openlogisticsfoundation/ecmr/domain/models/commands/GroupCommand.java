/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.models.commands;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GroupCommand {
    private Long id;
    private String name;
    private LocationCommand location;
}
