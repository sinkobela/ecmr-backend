/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.models;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ExternalUserRegistrationModel {
    @NotNull
    private UUID ecmrId;
    @NotNull
    private String shareToken;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private String company;
    private String email;
    private String phone;
}
