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
    private String nameCompany;
    @Size(min = 2, max = 60)
    private String namePerson;
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
    @Size(max = 15)
    @Pattern(regexp = "\\+?[0-9]{1,15}")
    private String phone;
}
