/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.web.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.openlogisticsfoundation.ecmr.api.model.SealedDocument;
import org.openlogisticsfoundation.ecmr.domain.exceptions.*;
import org.openlogisticsfoundation.ecmr.domain.models.*;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrShareService;
import org.openlogisticsfoundation.ecmr.web.exceptions.AuthenticationException;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/external")
@RequiredArgsConstructor
public class ExternalController {

    private final AuthenticationService authenticationService;
    private final EcmrShareService ecmrShareService;

    @GetMapping(path = {"/ecmr/{ecmrId}/export"})
    @Operation(
        tags = "ECMR External",
        summary = "Export eCMR as sealed document with ID and share token",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR", required = true, schema = @Schema(type = "string", format = "uuid")),
            @Parameter(name = "shareToken", description = "Share token", required = true, schema = @Schema(type = "string"))
        },
        responses = {
            @ApiResponse(description = "The requested ecmr as sealed document",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SealedDocument.class))),
            @ApiResponse(description = "eCMR not found", responseCode = "404"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<EcmrExportResult> exportEcmrToExternal(@PathVariable(value = "ecmrId") UUID ecmrId,
                                                                 @RequestParam @Valid @NotNull String shareToken) {
        try {
            return ResponseEntity.ok(this.ecmrShareService.exportEcmrToExternal(ecmrId, shareToken));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @PostMapping(path = {"/ecmr/import"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR External",
        summary = "Import eCMR with ID, share token and url",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR", required = true, schema = @Schema(type = "string", format = "uuid")),
            @Parameter(name = "shareToken", description = "Share token", required = true, schema = @Schema(type = "string")),
            @Parameter(name = "groupIds", description = "Group IDs", required = true, schema = @Schema(type = "object")),
            @Parameter(name = "url", description = "URL of the external instance", required = true, schema = @Schema(type = "string"))
        },
        responses = {
            @ApiResponse(description = "eCMR was imported successfully", responseCode = "200"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403"),
            @ApiResponse(description = "Share token is invalid", responseCode = "400")
        })
    public ResponseEntity<Void> importEcmrFromExternal(@RequestParam @NotNull String url,
                                                       @RequestParam @NotNull UUID ecmrId,
                                                       @RequestParam @NotNull String shareToken,
                                                       @RequestParam(name = "groupId") List<Long> groupIds) {
        try {
            AuthenticatedUser authenticatedUser = this.authenticationService.getAuthenticatedUser(true);
            this.ecmrShareService.importEcmrFromExternal(url, ecmrId, shareToken, groupIds, authenticatedUser);
            return ResponseEntity.ok().build();
        } catch (InvalidInputException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (EcmrAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping("/ecmr/{ecmrId}/email")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR External",
        summary = "Send eCMR sharing token via email",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR", required = true, schema = @Schema(type = "string", format = "uuid")),
            @Parameter(name = "receiverEmail", description = "Email address of the receiver", required = true, schema = @Schema(type = "string", format = "email")),
            @Parameter(name = "ecmrRole", description = "Role of the user for the eCMR", required = true, schema = @Schema(implementation = EcmrRole.class))
        },
        responses = {
            @ApiResponse(description = "Email sent successfully", responseCode = "200", content = @Content(schema = @Schema(implementation = EcmrShareResponse.class))),
            @ApiResponse(description = "eCMR not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403"),
            @ApiResponse(description = "Sealed eCMR not found", responseCode = "400")
        })
    public ResponseEntity<EcmrShareResponse> sendEmail(@RequestParam String receiverEmail,
                                                       @PathVariable(value = "ecmrId") String ecmrId,
                                                       @RequestParam(name = "ecmrRole") @Valid @NotNull EcmrRole ecmrRole) {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            EcmrShareResponse response = this.ecmrShareService.sendTokenPerEmail(UUID.fromString(ecmrId), receiverEmail, ecmrRole, new InternalOrExternalUser(authenticatedUser.getUser()));
            return ResponseEntity.ok(response);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (SealedEcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
