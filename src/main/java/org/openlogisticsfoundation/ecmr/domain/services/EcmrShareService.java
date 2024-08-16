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
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.mappers.GroupPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrShareResponse;
import org.openlogisticsfoundation.ecmr.domain.models.ShareEcmrResult;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ExternalUserRegistrationCommand;
import org.openlogisticsfoundation.ecmr.domain.services.tan.MessageProviderException;
import org.openlogisticsfoundation.ecmr.domain.services.tan.PhoneMessageProvider;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrAssignmentEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.ExternalUserEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrAssignmentRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.ExternalUserRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EcmrShareService {
    private final EcmrService ecmrService;
    private final EcmrAssignmentRepository ecmrAssignmentRepository;
    private final ExternalUserRepository externalUserRepository;
    private final PhoneMessageProvider phoneMessageProvider;
    private final UserRepository userRepository;
    private final GroupPersistenceMapper groupPersistenceMapper;

    @Value("${app.frontend-url}")
    String frontendAddress;

    public void registerExternalUser(@Valid ExternalUserRegistrationCommand command) throws EcmrNotFoundException, ValidationException, MessageProviderException {
        if (StringUtils.isBlank(command.getPhone())) {
            // Currently only phone is supported. Sharing an ecmr via e-mail could be a security risk
            throw new ValidationException("Phone must be filled");
        }
        EcmrEntity ecmrEntity = ecmrService.getEcmrEntity(command.getEcmrId());
        if (!command.getShareToken().equals(ecmrEntity.getShareWithCarrierToken())) {
            // Currently this is only allowed for carriers
            throw new ValidationException("Only carriers can register external users");
        }
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
        assignmentEntity.setRole(EcmrRole.Carrier);
        ecmrAssignmentRepository.save(assignmentEntity);

        String ecmrLink = this.frontendAddress + "/ecmr-tan/{ecmrId}/{tan}".replace("{ecmrId}", command.getEcmrId().toString()).replace("{tan}", tan);
        String tanMessage = "Your tan code is " + tan + " Please enter your code or click on the following link: " + ecmrLink;
        this.phoneMessageProvider.sendMessage(command.getPhone(), tanMessage);
    }

    public EcmrShareResponse shareEcmr(AuthenticatedUser authenticatedUser, @Valid @NotNull UUID ecmrId, @Valid @NotNull String email,
            @Valid @NotNull EcmrRole role) throws EcmrNotFoundException {
        EcmrEntity ecmr = ecmrService.getEcmrEntity(ecmrId);
        Optional<UserEntity> userEntityOpt = userRepository.findByEmail(email);
        if (userEntityOpt.isPresent()) {
            UserEntity userEntity = userEntityOpt.get();
            if (userEntity.getDefaultGroup() == null) {
                return new EcmrShareResponse(ShareEcmrResult.ErrorInternalUserHasNoGroup, null);
            } else {
                EcmrAssignmentEntity assignmentEntity = new EcmrAssignmentEntity();
                assignmentEntity.setEcmr(ecmr);
                assignmentEntity.setRole(role);
                assignmentEntity.setGroup(userEntity.getDefaultGroup());
                ecmrAssignmentRepository.save(assignmentEntity);
                return new EcmrShareResponse(ShareEcmrResult.SharedInternal, this.groupPersistenceMapper.toGroup(userEntity.getDefaultGroup()));
            }
        } else {
            //TODO Implement external sharing
            throw new NotImplementedException();
        }
    }

}
