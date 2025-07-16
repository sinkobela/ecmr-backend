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
public abstract class EcmrMemberCommand {
    private String companyName;
    @Size(min = 2, max = 60)
    private String personName;
    @Size(min = 2, max = 255)
    private String street;
    @Size(min = 2, max = 17)
    private String postcode;
    @Size(min = 2, max = 60)
    private String city;
    @Size(min = 2, max = 2)
    private String countryCode;
    @Size(max = 255)
    private String email;
    @Size(max = 32)
    @Pattern(regexp = "\\+?[0-9]{1,32}")
    private String phone;
}
