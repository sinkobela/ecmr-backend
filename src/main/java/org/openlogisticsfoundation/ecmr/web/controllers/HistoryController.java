/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import java.util.List;
import java.util.UUID;

import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.HistoryLog;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.domain.services.HistoryLogService;
import org.openlogisticsfoundation.ecmr.web.exceptions.AuthenticationException;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final AuthenticationService authenticationService;
    private final HistoryLogService historyLogService;

    @GetMapping(path = { "{ecmrId}" })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HistoryLog>> getHistoryLogs(@PathVariable(value = "ecmrId") UUID ecmrId) {
        try {
            AuthenticatedUser authenticatedUser = this.authenticationService.getAuthenticatedUser();
            List<HistoryLog> historyLogs = this.historyLogService.getLogs(ecmrId, new InternalOrExternalUser(authenticatedUser.getUser()));
            return ResponseEntity.ok(historyLogs);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }
}
