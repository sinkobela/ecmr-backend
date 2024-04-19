/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.web.controllers;

import org.openlogisticsfoundation.ecmr.web.models.EcmrModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ecmr")
@RequiredArgsConstructor
public class EcmrController {

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EcmrModel> getAuthenticatedUser() {
        return ResponseEntity.ok(new EcmrModel(1));
    }
}
