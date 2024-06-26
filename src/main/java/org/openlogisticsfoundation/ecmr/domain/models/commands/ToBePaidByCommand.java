/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.models.commands;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ToBePaidByCommand {
    @Valid
    private CustomChargeCommand customChargeCarriage;
    @Valid
    private CustomChargeCommand customChargeSupplementary;
    @Valid
    private CustomChargeCommand customChargeCustomsDuties;
    @Valid
    private CustomChargeCommand customChargeOther;
}
