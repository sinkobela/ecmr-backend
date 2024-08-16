/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.models.commands;

import java.util.List;

import org.openlogisticsfoundation.ecmr.domain.models.CountryCode;
import org.openlogisticsfoundation.ecmr.domain.models.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserCommand {
    @NotNull
    private UserRole role;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private CountryCode country;
    @NotNull
    @Email
    private String email;
    private String phone;
    @NotNull
    private List<Long> groupIds;
    private Long defaultGroupId;
}
