/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ExternalUserRegistrationCommand;
import org.openlogisticsfoundation.ecmr.domain.services.tan.MessageProviderException;
import org.openlogisticsfoundation.ecmr.domain.services.tan.PhoneMessageProvider;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrAssignmentEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.ExternalUserEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrAssignmentRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.ExternalUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExternalUserService {
    private final EcmrService ecmrService;
    private final EcmrAssignmentRepository ecmrAssignmentRepository;
    private final ExternalUserRepository externalUserRepository;
    private final PhoneMessageProvider phoneMessageProvider;
    @Value("${app.frontend-url}")
    String frontendAddress;

    public void registerExternalUser(ExternalUserRegistrationCommand command) throws EcmrNotFoundException, ValidationException, MessageProviderException {
        if (command.getRole() != EcmrRole.Carrier) {
            // Currently this is only allowed for carriers
            throw new ValidationException("Only carriers can register external users");
        }
        if (StringUtils.isBlank(command.getPhone())) {
            // Currently only phone is supported. Sharing an ecmr via e-mail could be a security risk
            throw new ValidationException("Phone must be filled");
        }
        EcmrEntity ecmrEntity = ecmrService.getEcmrEntity(command.getEcmrId());
        String tan = RandomStringUtils.randomNumeric(6);
        Instant tanValidUntil = Instant.now().plus(365, ChronoUnit.DAYS);
        ExternalUserEntity externalUserEntity = new ExternalUserEntity();
        externalUserEntity.setFirstName(command.getFirstName());
        externalUserEntity.setLastName(command.getLastName());
        externalUserEntity.setCompany(command.getCompany());
        externalUserEntity.setPhone(command.getPhone());
        externalUserEntity.setEmail(command.getEmail());
        externalUserEntity.setTan(tan);
        externalUserEntity.setTanValidUntil(tanValidUntil);
        externalUserEntity = externalUserRepository.save(externalUserEntity);
        EcmrAssignmentEntity assignmentEntity = new EcmrAssignmentEntity();
        assignmentEntity.setExternalUser(externalUserEntity);
        assignmentEntity.setEcmr(ecmrEntity);
        assignmentEntity.setRole(command.getRole());
        ecmrAssignmentRepository.save(assignmentEntity);

        String ecmrLink = this.frontendAddress + "/ecmr-tan/{ecmrId}/{tan}".replace("{ecmrId}", command.getEcmrId().toString()).replace("{tan}", tan);
        String tanMessage = "Your tan code is " + tan + " Please enter your code or click on the following link: " + ecmrLink;
        this.phoneMessageProvider.sendMessage(command.getPhone(), tanMessage);
    }

}
