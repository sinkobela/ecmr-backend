/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ExternalUserRegistrationCommand;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrAssignmentRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExternalUserService {
    private final EcmrService ecmrService;
    private final EcmrAssignmentRepository ecmrAssignmentRepository;

    public void registerExternalUser(ExternalUserRegistrationCommand command) throws EcmrNotFoundException, ValidationException {
        if (StringUtils.isBlank(command.getPhone()) && StringUtils.isBlank(command.getEmail())) {
            throw new ValidationException("Email or phone must be filled");
        }

        EcmrModel ecmr = ecmrService.getEcmr(command.getEcmrId());
    }


}
