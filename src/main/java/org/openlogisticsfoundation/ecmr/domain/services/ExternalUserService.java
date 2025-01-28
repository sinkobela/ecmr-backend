/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.Objects;
import java.util.UUID;

import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ExternalUserInvalidTanException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ExternalUserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.mappers.ExternalUserPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.ExternalUser;
import org.openlogisticsfoundation.ecmr.persistence.entities.ExternalUserEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.ExternalUserRepository;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ExternalUserService {
    private final EcmrRepository ecmrRepository;
    private final AuthorisationService authorisationService;
    private final ExternalUserPersistenceMapper externalUserPersistenceMapper;
    private final ExternalUserRepository externalUserRepository;
    private final int maximumInvalidTanCount = 10;

    public ExternalUser findExternalUser(UUID ecmrId, String userToken, String tan) throws ExternalUserNotFoundException, ExternalUserInvalidTanException {
        ExternalUserEntity externalUser = this.externalUserRepository.findExtenalUserByUserTokenAndEcmrId(userToken, ecmrId)
                .orElseThrow(() -> new ExternalUserNotFoundException(userToken));

        if (!Objects.equals(externalUser.getTan(), tan)) {
            externalUser.setInvalidLoginCount(externalUser.getInvalidLoginCount() + 1);
            externalUser.setActive(externalUser.isActive() && externalUser.getInvalidLoginCount() < maximumInvalidTanCount);
            externalUserRepository.save(externalUser);
            throw new ExternalUserInvalidTanException(userToken);
        }

        return externalUserPersistenceMapper.toDomain(externalUser);
    }

    public boolean isTanValid(@Valid @NotNull UUID ecmrId, @Valid @NotNull String userToken, @Valid @NotNull String tan) throws EcmrNotFoundException {
        if (!ecmrRepository.existsByEcmrId(ecmrId)) {
            throw new EcmrNotFoundException(ecmrId);
        }
        return authorisationService.tanValid(ecmrId, userToken, tan);
    }
}
