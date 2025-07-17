/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import java.util.UUID;

import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.SealedDocumentNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.SealedDocumentWithoutEcmr;
import org.openlogisticsfoundation.ecmr.domain.services.SealedDocumentService;
import org.openlogisticsfoundation.ecmr.web.exceptions.AuthenticationException;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sealed-document")
@RequiredArgsConstructor
public class SealedDocumentController {

    private final AuthenticationService authenticationService;
    private final SealedDocumentService sealedDocumentService;

    /**
     * Retrieves a specific sealed Document by the eCMR ID
     *
     * @param ecmrId The ID of the eCMR
     * @return The requested eCMR
     */
    @GetMapping("/{ecmrId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            tags = "Sealed Document",
            summary = "Retrieve Sealed Document without eCMR Model by eCMR ID",
            parameters = {
                    @Parameter(name = "ecmrId", description = "UUID of the eCMR", required = true, schema = @Schema(type = "string", format = "uuid"))
            },
            responses = {
                    @ApiResponse(description = "The requested sealed Document without eCMR Model",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SealedDocumentWithoutEcmr.class))),
                    @ApiResponse(description = "Sealed Document not found", responseCode = "404"),
                    @ApiResponse(description = "Unauthorized access", responseCode = "401"),
                    @ApiResponse(description = "Forbidden access", responseCode = "403")
            })
    public ResponseEntity<SealedDocumentWithoutEcmr> getSealedDocumentWithoutEcmr(@PathVariable(value = "ecmrId") UUID ecmrId) {
        try {
            AuthenticatedUser authenticatedUser = this.authenticationService.getAuthenticatedUser();
            SealedDocumentWithoutEcmr sealedDocumentWithoutEcmr = this.sealedDocumentService.getSealedDocumentWithoutEcmr(ecmrId,
                    new InternalOrExternalUser(authenticatedUser.getUser()));
            return ResponseEntity.ok(sealedDocumentWithoutEcmr);
        } catch (SealedDocumentNotFoundException e) {
            //Use no ResponseStatusException because we do not want a log, because this will happen regularly and is no error.
            return ResponseEntity.notFound().build();
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }
}
