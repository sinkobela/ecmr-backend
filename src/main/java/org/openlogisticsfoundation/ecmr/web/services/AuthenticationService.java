/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.web.services;

import java.util.Optional;
import java.util.UUID;

import org.openlogisticsfoundation.ecmr.domain.exceptions.ExternalUserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.UserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.ExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.User;
import org.openlogisticsfoundation.ecmr.domain.services.ExternalUserService;
import org.openlogisticsfoundation.ecmr.domain.services.UserService;
import org.openlogisticsfoundation.ecmr.web.exceptions.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final ExternalUserService externalUserService;

    public AuthenticatedUser getAuthenticatedUser() throws AuthenticationException {
        return this.getAuthenticatedUser(false);
    }

    public AuthenticatedUser getAuthenticatedUser(boolean withTechnical) throws AuthenticationException {
        User user = this.getUser();
        if (user.isTechnical() && !withTechnical) {
            throw new AuthenticationException("Technical User not allowed");
        }
        return new AuthenticatedUser(user);
    }

    private User getUser() throws AuthenticationException {
        Authentication authentication = this.getAuthentication();
        Object principal = Optional.ofNullable(authentication.getPrincipal())
                .orElseThrow(() -> new AuthenticationException("Authentication has no principal"));

        return switch (principal) {
            case Jwt jwt -> this.getUserFromJwt(jwt);
            case User user -> user;
            default -> throw new AuthenticationException("Principal is not of type Jwt or ApiKeyAuthentication");
        };
    }

    private User getUserFromJwt(Jwt jwt) throws AuthenticationException {
        Optional<String> emailClaim = Optional.ofNullable(jwt.getClaimAsString("email"));
        Optional<String> upnClaim = Optional.ofNullable(jwt.getClaimAsString("upn"));
        String email;
        if (emailClaim.isPresent()) {
            email = emailClaim.get();
        } else if (upnClaim.isPresent()) {
            email = upnClaim.get();
        } else {
            throw new AuthenticationException("Authentication has no claim of type email or upn");
        }

        try {
            return this.userService.getActiveUserByEmail(email);
        } catch (UserNotFoundException e) {
            throw new AuthenticationException("No active user found for email: " + email);
        }
    }

    public ExternalUser getExternalUser(UUID ecmrId, String tan) throws ExternalUserNotFoundException {
        return this.externalUserService.findExternalUser(ecmrId, tan);
    }

    private Authentication getAuthentication() throws AuthenticationException {
        SecurityContext securityContext = Optional.ofNullable(SecurityContextHolder.getContext())
                .orElseThrow(() -> new AuthenticationException("SecurityContext is empty"));
        return Optional.ofNullable(securityContext.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .orElseThrow(() -> new AuthenticationException("Authentication has isAuthenticated set to false"));
    }
}
