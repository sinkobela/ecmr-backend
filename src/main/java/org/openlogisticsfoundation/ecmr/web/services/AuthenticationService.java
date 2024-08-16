/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.web.services;

import java.util.Optional;

import org.openlogisticsfoundation.ecmr.domain.exceptions.UserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.User;
import org.openlogisticsfoundation.ecmr.domain.services.UserService;
import org.openlogisticsfoundation.ecmr.web.exceptions.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserService userService;

    public AuthenticationService(UserService userService) {
        this.userService = userService;
    }

    public AuthenticatedUser getAuthenticatedUser() throws AuthenticationException {
        Authentication authentication = this.getAuthentication();
        Jwt jwt = this.getJwt(authentication);
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
            User user = this.userService.getUserByEmail(email);
            return new AuthenticatedUser(user);
        } catch (UserNotFoundException e) {
            throw new AuthenticationException("No user found for email: " + email);
        }
    }

    private Authentication getAuthentication() throws AuthenticationException {
        SecurityContext securityContext = Optional.ofNullable(SecurityContextHolder.getContext())
                .orElseThrow(() -> new AuthenticationException("SecurityContext is empty"));
        return Optional.ofNullable(securityContext.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .orElseThrow(() -> new AuthenticationException("Authentication has isAuthenticated set to false"));
    }

    private Jwt getJwt(Authentication authorization) throws AuthenticationException {
        Object principal = Optional.ofNullable(authorization.getPrincipal())
                .orElseThrow(() -> new AuthenticationException("Authentication has no principal"));
        if (principal instanceof Jwt jwt) {
            return jwt;
        }
        throw new AuthenticationException("Principal is not of type Jwt");
    }
}
