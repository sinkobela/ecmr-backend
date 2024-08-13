/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.services;

import java.time.Instant;

import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.exceptions.UserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.mappers.UserPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.UserToEcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.UserRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.UserToEcmrRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EcmrCreationService {
    private final EcmrPersistenceMapper persistenceMapper;
    private final EcmrRepository ecmrRepository;
    private final UserToEcmrRepository userToEcmrRepository;
    private final UserPersistenceMapper userPersistenceMapper;
    private final UserRepository userRepository;

    public void createEcmr(EcmrCommand ecmrCommand, AuthenticatedUser authenticatedUser) throws UserNotFoundException {
        this.createEcmr(ecmrCommand, EcmrType.ECMR, authenticatedUser);
    }

    public EcmrEntity createTemplate(EcmrCommand ecmrCommand, AuthenticatedUser authenticatedUser) throws UserNotFoundException {
        return this.createEcmr(ecmrCommand, EcmrType.TEMPLATE, authenticatedUser);
    }

    private EcmrEntity createEcmr(EcmrCommand ecmrCommand, EcmrType type, AuthenticatedUser authenticatedUser) throws UserNotFoundException {
        EcmrEntity ecmrEntity = this.persistenceMapper.toEntity(ecmrCommand, type, EcmrStatus.NEW);

        String fullName = String.format("%s %s", authenticatedUser.getUser().getFirstName(), authenticatedUser.getUser().getLastName());
        ecmrEntity.setCreatedBy(fullName);

        ecmrEntity.setCreatedAt(Instant.now());
        ecmrEntity = this.ecmrRepository.save(ecmrEntity);

        String email = authenticatedUser.getUser().getEmail();
        UserEntity userEntity = this.userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(authenticatedUser.getUser().getId()));
        this.assignUserToEcmr(ecmrEntity, userEntity);

        return ecmrEntity;
    }

    private void assignUserToEcmr(EcmrEntity ecmr, UserEntity user){
        UserToEcmrEntity userToEcmrEntity = new UserToEcmrEntity();
        userToEcmrEntity.setUser(user);
        userToEcmrEntity.setEcmr(ecmr);
        this.userToEcmrRepository.save(userToEcmrEntity);
    }
}
