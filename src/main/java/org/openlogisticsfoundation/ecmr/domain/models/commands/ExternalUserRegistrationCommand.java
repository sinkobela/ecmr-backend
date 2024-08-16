/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.models.commands;

import java.util.UUID;

import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ExternalUserRegistrationCommand {
    @NotNull
    private UUID ecmrId;
    @NotNull
    private EcmrRole role;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private String company;
    private String email;
    private String phone;
}
