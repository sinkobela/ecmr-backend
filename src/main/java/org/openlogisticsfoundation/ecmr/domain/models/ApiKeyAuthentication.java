/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.models;

import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import lombok.Getter;

@Getter
public class ApiKeyAuthentication extends AbstractAuthenticationToken {
    private final UUID apiKey;
    private final String apiKeyDescription;
    private final User user;

    public ApiKeyAuthentication(UUID apiKey, String apiKeyDescription, User user) {
        super(List.of());
        this.apiKey = apiKey;
        this.apiKeyDescription = apiKeyDescription;
        this.user = user;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }
}
