/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.Optional;
import java.util.UUID;

import org.openlogisticsfoundation.ecmr.domain.mappers.UserPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.ApiKeyAuthentication;
import org.openlogisticsfoundation.ecmr.persistence.entities.ApiKeyEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.ApiKeyRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiKeyAuthenticationService {
    private final ApiKeyRepository apiKeyRepository;
    private final UserPersistenceMapper userMapper;

    public Optional<ApiKeyAuthentication> getApiKeyAuthentication(UUID apiKey) {
        Optional<ApiKeyEntity> apiKeyEntity = this.apiKeyRepository.findByValue(apiKey);
        return apiKeyEntity.map(entity -> {
            if (entity.getUser().isDeactivated()) {
                return null;
            }
            return entity;
        }).map(entity -> new ApiKeyAuthentication(entity.getValue(), entity.getDescription(), userMapper.toUser(entity.getUser())));
    }
}
