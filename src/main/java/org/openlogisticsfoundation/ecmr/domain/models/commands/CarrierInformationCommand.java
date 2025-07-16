/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.models.commands;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class CarrierInformationCommand extends EcmrMemberCommand {
    private String carrierLicensePlate;
    @Size(max = 32)
    @Pattern(regexp = "\\+?[0-9]{1,32}")
    private String driverPhone;
}
