/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import static org.openlogisticsfoundation.ecmr.web.controllers.PdfHelper.createPdfResponse;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.NotImplementedException;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.signature.Signature;
import org.openlogisticsfoundation.ecmr.domain.exceptions.*;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrShareResponse;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.PdfFile;
import org.openlogisticsfoundation.ecmr.domain.models.SignatureType;
import org.openlogisticsfoundation.ecmr.domain.models.SortingField;
import org.openlogisticsfoundation.ecmr.domain.models.SortingOrder;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrCreationService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrShareService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrSignService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrUpdateService;
import org.openlogisticsfoundation.ecmr.web.exceptions.AuthenticationException;
import org.openlogisticsfoundation.ecmr.web.mappers.EcmrWebMapper;
import org.openlogisticsfoundation.ecmr.web.models.*;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ecmr")
@RequiredArgsConstructor
public class EcmrController {

    private final EcmrService ecmrService;
    private final EcmrUpdateService ecmrUpdateService;
    private final EcmrCreationService ecmrCreationService;
    private final EcmrWebMapper ecmrWebMapper;
    private final AuthenticationService authenticationService;
    private final EcmrShareService ecmrShareService;
    private final EcmrSignService ecmrSignService;

    /**
     * Retrieves a paginated list of eCMRs for the authenticated user
     *
     * @param type               The type of eCMRs
     * @param page               The page number for pagination
     * @param size               The size of the paginated list
     * @param sortBy             The column name used for sorting the results
     * @param sortingOrder       The sorting order (ASC/DESC)
     * @param filterRequestModel The filter criteria for eCMRs
     * @return A paginated list of eCMRs
     */
    @PostMapping("/my-ecmrs")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Retrieve My eCMRs",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = FilterRequestModel.class))),
        responses = {
            @ApiResponse(description = "Paginated list of eCMRs",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = EcmrPageModel.class)))
        })
    public ResponseEntity<EcmrPageModel> getMyEcmrs(@RequestParam(required = false, defaultValue = "ECMR") EcmrType type,
                                                    @RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                    @RequestParam(name = "size", defaultValue = "10", required = false) int size,
                                                    @RequestParam(name = "sortBy", defaultValue = "creationDate", required = false) SortingField sortBy,
                                                    @RequestParam(name = "sortingOrder", defaultValue = "ASC", required = false) SortingOrder sortingOrder,
                                                    @RequestBody FilterRequestModel filterRequestModel
    )
            throws AuthenticationException {
        AuthenticatedUser authenticatedUser = this.authenticationService.getAuthenticatedUser();
        EcmrPageModel pageModel = this.ecmrService.getEcmrsForUser(authenticatedUser, type, page, size, sortBy, sortingOrder,
                ecmrWebMapper.map(filterRequestModel));
        return ResponseEntity.ok(pageModel);
    }

    /**
     * Retrieves a specific eCMR by ID
     *
     * @param ecmrId The ID of the eCMR
     * @return The requested eCMR
     */
    @GetMapping(path = {"{ecmrId}"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Retrieve eCMR by ID",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR", required = true, schema = @Schema(type = "string", format = "uuid"))
        },
        responses = {
            @ApiResponse(description = "The requested eCMR",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = EcmrModel.class))),
            @ApiResponse(description = "eCMR not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<EcmrModel> getEcmr(@PathVariable(value = "ecmrId") UUID ecmrId) {
        try {
            AuthenticatedUser authenticatedUser = this.authenticationService.getAuthenticatedUser();
            EcmrModel ecmrModel = this.ecmrService.getEcmr(ecmrId, new InternalOrExternalUser(authenticatedUser.getUser()));
            return ResponseEntity.ok(ecmrModel);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Creates a new eCMR
     *
     * @param ecmrModel The eCMR data to create
     * @param groupIds  The IDs of the groups to associate with the eCMR
     * @return The created eCMR
     */
    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Create a new eCMR",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = EcmrModel.class))),
        responses = {
            @ApiResponse(description = "The created eCMR",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = EcmrModel.class))),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403"),
            @ApiResponse(description = "Bad request", responseCode = "400")
        })
    public ResponseEntity<EcmrModel> createEcmr(@RequestBody @Valid EcmrModel ecmrModel, @RequestParam(name = "groupId") List<Long> groupIds) {
        EcmrCommand ecmrCommand = ecmrWebMapper.toCommand(ecmrModel);
        EcmrModel createdEcmr;
        try {
            AuthenticatedUser authenticatedUser = this.authenticationService.getAuthenticatedUser(true);
            createdEcmr = this.ecmrCreationService.createEcmr(ecmrCommand, authenticatedUser, groupIds);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
        return ResponseEntity.ok(createdEcmr);
    }

    /**
     * Deletes an eCMR by ID
     *
     * @param ecmrId The ID of the eCMR to delete
     */
    @DeleteMapping("/{ecmrId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Delete an eCMR",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR to delete", required = true, schema = @Schema(type = "string", format = "uuid"))
        },
        responses = {
            @ApiResponse(description = "eCMR deleted successfully", responseCode = "204"),
            @ApiResponse(description = "eCMR not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public void deleteEcmr(@PathVariable(value = "ecmrId") UUID ecmrId) throws EcmrNotFoundException {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            ecmrService.deleteEcmr(ecmrId, new InternalOrExternalUser(authenticatedUser.getUser()));
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Archives an eCMR
     *
     * @param ecmrId The ID of the eCMR to archive
     * @return The archived eCMR
     */
    @PatchMapping(path = {"{ecmrId}/archive"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Archive an eCMR",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR to archive", required = true, schema = @Schema(type = "string", format = "uuid"))
        },
        responses = {
            @ApiResponse(description = "The archived eCMR",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = EcmrModel.class))),
            @ApiResponse(description = "eCMR not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<EcmrModel> archiveEcmr(@PathVariable(value = "ecmrId") UUID ecmrId) {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            EcmrModel result = this.ecmrUpdateService.archiveEcmr(ecmrId, authenticatedUser);
            return ResponseEntity.ok(result);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Reactivates an archived eCMR
     *
     * @param ecmrId The ID of the eCMR to reactivate
     * @return The reactivated eCMR
     */
    @PatchMapping(path = {"{ecmrId}/reactivate"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Reactivate an eCMR",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR to reactivate", required = true, schema = @Schema(type = "string", format = "uuid"))
        },
        responses = {
            @ApiResponse(description = "The reactivated eCMR",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = EcmrModel.class))),
            @ApiResponse(description = "eCMR not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<EcmrModel> reactivateEcmr(@PathVariable(value = "ecmrId") UUID ecmrId) {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            EcmrModel result = this.ecmrUpdateService.reactivateEcmr(ecmrId, authenticatedUser);
            return ResponseEntity.ok(result);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Shares an eCMR with a user
     *
     * @param ecmrId         The ID of the eCMR to share
     * @param ecmrShareModel The sharing details
     * @return The response of the sharing operation
     */
    @PatchMapping(path = {"{ecmrId}/share"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Share an eCMR",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR to share", required = true, schema = @Schema(type = "string", format = "uuid"))
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = EcmrShareModel.class))),
        responses = {
            @ApiResponse(description = "Successfully shared eCMR",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = EcmrShareResponse.class))),
            @ApiResponse(description = "eCMR not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403"),
            @ApiResponse(description = "Bad request", responseCode = "400")
        })
    public ResponseEntity<EcmrShareResponse> shareEcmr(@PathVariable(value = "ecmrId") UUID ecmrId,
            @RequestBody @Valid EcmrShareModel ecmrShareModel) {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            return ResponseEntity.ok(
                this.ecmrShareService.shareEcmr(new InternalOrExternalUser(authenticatedUser.getUser()), ecmrId, ecmrShareModel.getEmail(),
                    ecmrShareModel.getRole()));
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (NotImplementedException e) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    /**
     * Shares an eCMR with a group
     *
     * @param ecmrId         The ID of the eCMR to share
     * @param ecmrShareWithGroupModel The sharing details
     * @return The response of the sharing operation
     */
    @PatchMapping(path = {"{ecmrId}/shareWithGroup"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Share an eCMR",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR to share", required = true, schema = @Schema(type = "string", format = "uuid"))
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = EcmrShareWithGroupModel.class))),
        responses = {
            @ApiResponse(description = "Successfully shared eCMR",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = EcmrShareResponse.class))),
            @ApiResponse(description = "eCMR or group not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403"),
            @ApiResponse(description = "Bad request", responseCode = "400")
        })
    public ResponseEntity<EcmrShareResponse> shareEcmrWithGroup(@PathVariable(value = "ecmrId") UUID ecmrId,
                                                       @RequestBody @Valid EcmrShareWithGroupModel ecmrShareWithGroupModel) {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            return ResponseEntity.ok(
                this.ecmrShareService.shareEcmrWithGroup(new InternalOrExternalUser(authenticatedUser.getUser()), ecmrId, ecmrShareWithGroupModel.getGroupId(),
                    ecmrShareWithGroupModel.getRole()));
        } catch (EcmrNotFoundException | GroupNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (NotImplementedException e) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }


    /**
     * Imports an eCMR using a share token
     *
     * @param ecmrId     The ID of the eCMR to import
     * @param shareToken The token used for sharing
     * @return The imported eCMR
     */
    @GetMapping(path = {"{ecmrId}/import"})
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Import an eCMR",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR to import", required = true, schema = @Schema(type = "string", format = "uuid")),
            @Parameter(name = "shareToken", description = "Share token for importing the eCMR", required = true, schema = @Schema(type = "string"))
        },
        responses = {
            @ApiResponse(description = "The imported eCMR",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = EcmrModel.class))),
            @ApiResponse(description = "eCMR not found", responseCode = "404"),
            @ApiResponse(description = "User not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403"),
            @ApiResponse(description = "Not implemented", responseCode = "501")
        })
    public ResponseEntity<EcmrModel> importEcmr(@PathVariable(value = "ecmrId") UUID ecmrId, @RequestParam @Valid @NotNull String shareToken) {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            return ResponseEntity.ok(this.ecmrShareService.importEcmr(authenticatedUser, ecmrId, shareToken));
        } catch (EcmrNotFoundException | UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (NotImplementedException e) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, e.getMessage());
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    /**
     * Downloads the PDF file of the eCMR
     *
     * @param id The ID of the eCMR
     * @return The PDF file of the eCMR
     */
    @GetMapping("/{ecmrId}/pdf")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Download eCMR PDF",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR to download", required = true, schema = @Schema(type = "string", format = "uuid"))
        },
        responses = {
            @ApiResponse(description = "PDF file of the eCMR",
                content = @Content(
                    mediaType = "application/pdf")),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403"),
            @ApiResponse(description = "eCMR not found", responseCode = "404"),
            @ApiResponse(description = "Error creating PDF", responseCode = "500")
        })
    public ResponseEntity<StreamingResponseBody> downloadEcmrPdfFile(@PathVariable("ecmrId") UUID id) {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser(true);
            PdfFile ecmrReport = this.ecmrService.createJasperReportForEcmr(id, new InternalOrExternalUser(authenticatedUser.getUser()), true);
            return createPdfResponse(ecmrReport);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (PdfCreationException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Updates an existing eCMR
     *
     * @param ecmrModel The updated eCMR data
     * @return The updated eCMR
     */
    @PutMapping()
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Update an existing eCMR",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = EcmrModel.class))),
        responses = {
            @ApiResponse(description = "The updated eCMR",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = EcmrModel.class))),
            @ApiResponse(description = "eCMR not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<EcmrModel> updateEcmr(@RequestBody @Valid EcmrModel ecmrModel) {
        try {
            AuthenticatedUser authenticatedUser = this.authenticationService.getAuthenticatedUser();
            UUID ecmrId = UUID.fromString(ecmrModel.getEcmrId());
            EcmrCommand ecmrCommand = ecmrWebMapper.toCommand(ecmrModel);
            EcmrModel result = this.ecmrUpdateService.updateEcmr(ecmrCommand, ecmrId, new InternalOrExternalUser(authenticatedUser.getUser()));
            return ResponseEntity.ok(result);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Signs the eCMR on glass
     *
     * @param ecmrId    The ID of the eCMR
     * @param signModel The signature model
     * @return The created signature
     */
    @PostMapping("/{ecmrId}/sign-on-glass")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Sign eCMR on glass",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR to sign", required = true, schema = @Schema(type = "string", format = "uuid"))
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = SignModel.class))),
        responses = {
            @ApiResponse(description = "The created signature",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Signature.class))),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "eCMR not found", responseCode = "404"),
            @ApiResponse(description = "Validation error or signature already present", responseCode = "400"),
            @ApiResponse(description = "Forbidden access", responseCode =
                "403")
        })
    public ResponseEntity<Signature> signOnGlass(@PathVariable(value = "ecmrId") UUID ecmrId, @RequestBody @Valid @NotNull SignModel signModel) {
        try {
            AuthenticatedUser authenticatedUser = this.authenticationService.getAuthenticatedUser();
            return ResponseEntity.ok(this.ecmrSignService.signEcmr(new InternalOrExternalUser(authenticatedUser.getUser()), ecmrId,
                    ecmrWebMapper.map(signModel), SignatureType.SignOnGlass));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ValidationException | SignatureAlreadyPresentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Signs the eCMR on glass
     *
     * @param ecmrId    The ID of the eCMR
     * @param sealModel The seal model
     * @return The created signature
     */
    @PostMapping("/{ecmrId}/seal")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Seal eCMR",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR to seal", required = true, schema = @Schema(type = "string", format = "uuid"))
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = SealModel.class))),
        responses = {
            @ApiResponse(description = "The created signature",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Signature.class))),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "eCMR not found", responseCode = "404"),
            @ApiResponse(description = "Validation error or signature already present", responseCode = "400"),
            @ApiResponse(description = "Forbidden access", responseCode =
                "403")
        })
    public ResponseEntity<Signature> seal(@PathVariable(value = "ecmrId") UUID ecmrId, @RequestBody @Valid @NotNull SealModel sealModel) {
        try {
            AuthenticatedUser authenticatedUser = this.authenticationService.getAuthenticatedUser();
            return ResponseEntity.ok(this.ecmrSignService.sealEcmr(new InternalOrExternalUser(authenticatedUser.getUser()), ecmrId,
                ecmrWebMapper.map(sealModel), SignatureType.ESeal));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ValidationException | SignatureAlreadyPresentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Retrieves the share token for an eCMR
     *
     * @param id       The ID of the eCMR
     * @param ecmrRole The role for sharing
     * @return The share token
     */
    @GetMapping("{ecmrId}/share-token")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Get share token for eCMR",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR", required = true, schema = @Schema(type = "string", format = "uuid")),
            @Parameter(name = "ecmrRole", description = "Role for sharing the eCMR", required = true, schema = @Schema(type = "string"))
        },
        responses = {
            @ApiResponse(description = "The share token",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "string"))),
            @ApiResponse(description = "eCMR not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401"),
            @ApiResponse(description = "Forbidden access", responseCode = "403")
        })
    public ResponseEntity<String> getShareToken(@PathVariable(value = "ecmrId") UUID id,
            @RequestParam(name = "ecmrRole") @Valid @NotNull EcmrRole ecmrRole) {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            return ResponseEntity.ok(this.ecmrShareService.getShareToken(id, ecmrRole, new InternalOrExternalUser(authenticatedUser.getUser())));
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Retrieves the current roles for an eCMR
     *
     * @param ecmrId The ID of the eCMR
     * @return The list of current roles
     */
    @GetMapping("/{ecmrId}/role")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "ECMR",
        summary = "Get current eCMR roles",
        parameters = {
            @Parameter(name = "ecmrId", description = "UUID of the eCMR", required = true, schema = @Schema(type = "string", format = "uuid"))
        },
        responses = {
            @ApiResponse(description = "List of current roles",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = EcmrRole.class))),
            @ApiResponse(description = "eCMR not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401")
        })
    public ResponseEntity<List<EcmrRole>> getCurrentEcmrRoles(@PathVariable(name = "ecmrId") UUID ecmrId) {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            return ResponseEntity.ok(this.ecmrService.getCurrentEcmrRoles(ecmrId, new InternalOrExternalUser(authenticatedUser.getUser())));
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
}
