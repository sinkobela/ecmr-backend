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

import org.apache.commons.lang3.NotImplementedException;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.areas.six.CarrierInformation;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ExternalUserInvalidTanException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ExternalUserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.PdfCreationException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.RateLimitException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.SealAlreadyPresentException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.SealedDocumentNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrShareResponse;
import org.openlogisticsfoundation.ecmr.domain.models.ExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.PdfFile;
import org.openlogisticsfoundation.ecmr.domain.models.SealedDocumentWithoutEcmr;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ExternalUserRegistrationCommand;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrPdfService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrSealService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrShareService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrUpdateService;
import org.openlogisticsfoundation.ecmr.domain.services.ExternalUserService;
import org.openlogisticsfoundation.ecmr.domain.services.SealedDocumentService;
import org.openlogisticsfoundation.ecmr.domain.services.tan.MessageProviderException;
import org.openlogisticsfoundation.ecmr.web.mappers.EcmrWebMapper;
import org.openlogisticsfoundation.ecmr.web.mappers.ExternalUserWebMapper;
import org.openlogisticsfoundation.ecmr.web.models.EcmrShareModel;
import org.openlogisticsfoundation.ecmr.web.models.ExternalUserRegistrationModel;
import org.openlogisticsfoundation.ecmr.web.models.ExternalUserRegistrationResponseModel;
import org.openlogisticsfoundation.ecmr.web.models.SealModel;
import org.openlogisticsfoundation.ecmr.web.models.SharedCarrierInformationModel;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/anonymous")
@RequiredArgsConstructor
public class AnonymousController {
    private final EcmrShareService ecmrShareService;
    private final ExternalUserWebMapper externalUserWebMapper;
    private final ExternalUserService externalUserService;
    private final AuthenticationService authenticationService;
    private final EcmrService ecmrService;
    private final EcmrWebMapper ecmrWebMapper;
    private final EcmrUpdateService ecmrUpdateService;
    private final SealedDocumentService sealedDocumentService;
    private final EcmrSealService ecmrSealService;
    private final EcmrPdfService ecmrPdfService;

    /**
     * Checks if the provided TAN is valid for a given ECMR ID.
     *
     * @param ecmrId The UUID of the ECMR.
     * @param tan The TAN to validate.
     * @return True if the TAN is valid, otherwise false.
     */
    @GetMapping("/is-tan-valid")
    @Operation(
            tags = "Anonymous",
            summary = "Check TAN Validity",
            parameters = {
                    @Parameter(name = "ecmrId", description = "UUID of the ECMR", required = true, schema = @Schema(type = "string", format = "uuid")),
                    @Parameter(name = "tan", description = "TAN to validate", required = true, schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(description = "Validity of the TAN",
                            content = @Content(mediaType = "application/json", schema = @Schema(type = "boolean"))),
                    @ApiResponse(description = "ECMR not found", responseCode = "404"),
            })
    public ResponseEntity<Boolean> isTanValid(@RequestParam(name = "ecmrId") @Valid @NotNull UUID ecmrId,
            @RequestParam(name = "userToken") @NotNull @Valid String userToken,
            @RequestParam(name = "tan") @NotNull @Valid String tan) {
        try {
            return ResponseEntity.ok(this.externalUserService.isTanValid(ecmrId, userToken, tan));
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Registers an external user.
     *
     * @param externalUserRegistrationModel The registration details of the external user.
     * @return User token
     */
    @PostMapping("/registration")
    @Operation(
            tags = "Anonymous",
            summary = "Register External User",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExternalUserRegistrationModel.class))),
            responses = {
                    @ApiResponse(description = "User registered successfully", responseCode = "200"),
                    @ApiResponse(description = "ECMR not found", responseCode = "404"),
                    @ApiResponse(description = "Validation error", responseCode = "400"),
                    @ApiResponse(description = "Internal server error", responseCode = "500")
            })
    public ResponseEntity<ExternalUserRegistrationResponseModel> registerExternalUser(
            @Valid @RequestBody ExternalUserRegistrationModel externalUserRegistrationModel) {
        try {
            ExternalUserRegistrationCommand command = externalUserWebMapper.map(externalUserRegistrationModel);
            String userToken = this.ecmrShareService.registerExternalUser(command);
            return ResponseEntity.ok(new ExternalUserRegistrationResponseModel(command.getEcmrId(), userToken));
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (MessageProviderException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (RateLimitException e) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
        }
    }

    /**
     * Retrieves ECMR carrier details for a given ECMR ID
     *
     * @param ecmrId The UUID of the ECMR.
     * @param ecmrToken The carrier's share token of the ECMR.
     * @return the ECMR carrier information.
     */
    @GetMapping(path = { "/ecmr-carrier/{ecmrId}/{ecmrToken}" })
    @Operation(
            tags = "Anonymous",
            summary = "Get ECMR carrier information",
            parameters = {
                    @Parameter(name = "ecmrId", description = "UUID of the ECMR", required = true,
                            schema = @Schema(type = "string", format = "uuid")),
                    @Parameter(name = "ecmrToken", description = "shareToken of the ECMR", required = true,
                            schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(description = "ECMR carrier details", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SharedCarrierInformationModel.class))),
                    @ApiResponse(description = "ECMR not found", responseCode = "404"),
                    @ApiResponse(description = "Validation error", responseCode = "400")
            })
    public ResponseEntity<SharedCarrierInformationModel> getEcmrCarrierInfo(@PathVariable(value = "ecmrId") UUID ecmrId,
            @PathVariable(value = "ecmrToken") String ecmrToken) {
        try {
            CarrierInformation ecmrCarrierInformation = ecmrShareService.getEcmrCarrierInformation(ecmrId, ecmrToken);
            SharedCarrierInformationModel sharedCarrierInformation =
                    ecmrWebMapper.toSharedCarrierInformation(ecmrCarrierInformation);
            return ResponseEntity.ok(sharedCarrierInformation);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Retrieves ECMR details for a given ECMR ID.
     *
     * @param ecmrId The UUID of the ECMR.
     * @param userToken Unique token of the external user
     * @param tan The TAN for validation.
     * @return The ECMR model.
     */
    @GetMapping(path = { "/ecmr/{ecmrId}" })
    @Operation(
            tags = "Anonymous",
            summary = "Get ECMR Details",
            parameters = {
                    @Parameter(name = "ecmrId", description = "UUID of the ECMR", required = true, schema = @Schema(type = "string", format = "uuid")),
                    @Parameter(name = "tan", description = "TAN for validation", required = true, schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(description = "ECMR details",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EcmrModel.class))),
                    @ApiResponse(description = "ECMR not found", responseCode = "404"),
                    @ApiResponse(description = "No permission", responseCode = "403"),
                    @ApiResponse(description = "External user not found", responseCode = "401")
            })
    public ResponseEntity<EcmrModel> getEcmrWith(@PathVariable(value = "ecmrId") UUID ecmrId,
            @RequestParam(name = "userToken") @Valid @NotNull String userToken, @RequestParam(name = "tan") @Valid @NotNull String tan) {
        try {
            ExternalUser externalUser = authenticationService.getExternalUser(ecmrId, userToken, tan);
            EcmrModel ecmrModel = this.ecmrService.getEcmr(ecmrId, new InternalOrExternalUser(externalUser));
            return ResponseEntity.ok(ecmrModel);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (ExternalUserNotFoundException | ExternalUserInvalidTanException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    /**
     * Updates an ECMR.
     *
     * @param tan The TAN for validation.
     * @param userToken Unique token of the external user.
     * @param ecmrModel The ECMR model to update.
     * @return The updated ECMR model.
     */
    @PutMapping("/ecmr")
    @Operation(
            tags = "Anonymous",
            summary = "Update ECMR",
            parameters = {
                    @Parameter(name = "userToken", description = "Unique token of the external user", required = true, schema = @Schema(type = "string")),
                    @Parameter(name = "tan", description = "TAN for validation", required = true, schema = @Schema(type = "string"))
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EcmrModel.class))),
            responses = {
                    @ApiResponse(description = "Updated ECMR model",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EcmrModel.class))),
                    @ApiResponse(description = "ECMR not found", responseCode = "404"),
                    @ApiResponse(description = "No permission", responseCode = "403"),
                    @ApiResponse(description = "External user not found", responseCode = "401")
            })
    public ResponseEntity<EcmrModel> updateEcmr(@RequestParam(name = "userToken") @Valid @NotNull String userToken,
            @RequestParam(name = "tan") @Valid @NotNull String tan,
            @RequestBody EcmrModel ecmrModel) {
        try {
            UUID ecmrId = UUID.fromString(ecmrModel.getEcmrId());
            ExternalUser externalUser = this.authenticationService.getExternalUser(ecmrId, userToken, tan);
            EcmrCommand ecmrCommand = ecmrWebMapper.toCommand(ecmrModel);
            EcmrModel result = this.ecmrUpdateService.updateEcmr(ecmrCommand, ecmrId, new InternalOrExternalUser(externalUser));
            return ResponseEntity.ok(result);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (ExternalUserNotFoundException | ExternalUserInvalidTanException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    /**
     * Seals an ECMR.
     *
     * @param ecmrId The UUID of the ECMR.
     * @param userToken Unique token of the external user
     * @param tan The TAN for validation.
     * @param sealModel The seal model containing seal data.
     */
    @PostMapping("/ecmr/{ecmrId}/seal")
    @Operation(
            tags = "Anonymous",
            summary = "Seal ECMR",
            parameters = {
                    @Parameter(name = "ecmrId", description = "UUID of the ECMRto seal", required = true, schema = @Schema(type = "string", format =
                            "uuid")),
                    @Parameter(name = "userToken", description = "Unique token of the external user", required = true, schema = @Schema(type = "string")),
                    @Parameter(name = "tan", description = "TAN for validation", required = true, schema = @Schema(type = "string"))
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SealModel.class))),
            responses = {
                    @ApiResponse(description = "ECMR not found", responseCode = "404"),
                    @ApiResponse(description = "Forbidden access", responseCode = "403"),
                    @ApiResponse(description = "External user not found", responseCode = "401"),
                    @ApiResponse(description = "Validation error or seal already present", responseCode = "400")
            })
    public ResponseEntity<Void> seal(@PathVariable(value = "ecmrId") UUID ecmrId,
            @RequestParam(name = "userToken") @Valid @NotNull String userToken, @RequestParam(name = "tan") @Valid @NotNull String tan,
            @RequestBody @Valid @NotNull SealModel sealModel) {
        try {
            ExternalUser externalUser = this.authenticationService.getExternalUser(ecmrId, userToken, tan);
            this.ecmrSealService.sealEcmr(ecmrId, ecmrWebMapper.map(sealModel), new InternalOrExternalUser(externalUser));
            return ResponseEntity.ok().build();
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ValidationException | SealAlreadyPresentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (ExternalUserNotFoundException | ExternalUserInvalidTanException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    /**
     * Retrieves a share token for an ECMR.
     *
     * @param ecmrId The UUID of the ECMR.
     * @param userToken Unique token of the external user
     * @param tan The TAN for validation.
     * @param ecmrRole The role for the ECMR.
     * @return The share token.
     */
    @GetMapping("/ecmr/{ecmrId}/share-token")
    @Operation(
            tags = "Anonymous",
            summary = "Get Share Token for ECMR",
            parameters = {
                    @Parameter(name = "ecmrId", description = "UUID of the ECMR", required = true, schema = @Schema(type = "string", format = "uuid")),
                    @Parameter(name = "userToken", description = "Unique token of the external user", required = true, schema = @Schema(type = "string")),
                    @Parameter(name = "tan", description = "TAN for validation", required = true, schema = @Schema(type = "string")),
                    @Parameter(name = "ecmrRole", description = "Role for the ECMR", required = true, schema = @Schema(implementation = EcmrRole.class))
            },
            responses = {
                    @ApiResponse(description = "Share token",
                            content = @Content(mediaType = "application/json", schema = @Schema(type = "string"))),
                    @ApiResponse(description = "ECMR not found", responseCode = "404"),
                    @ApiResponse(description = "No permission", responseCode = "403"),
                    @ApiResponse(description = "External user not found", responseCode = "401")
            })
    public ResponseEntity<String> getShareToken(@PathVariable(value = "ecmrId") UUID ecmrId, @RequestParam(name = "tan") @Valid @NotNull String tan,
            @RequestParam(name = "userToken") @Valid @NotNull String userToken, @RequestParam(name = "ecmrRole") @Valid @NotNull EcmrRole ecmrRole) {
        try {
            ExternalUser externalUser = this.authenticationService.getExternalUser(ecmrId, userToken, tan);
            return ResponseEntity.ok(this.ecmrShareService.getShareToken(ecmrId, ecmrRole, new InternalOrExternalUser(externalUser)));
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (ExternalUserNotFoundException | ExternalUserInvalidTanException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    /**
     * Shares an ECMR.
     *
     * @param ecmrId The UUID of the ECMR.
     * @param userToken Unique token of the external user
     * @param tan The TAN for validation.
     * @param ecmrShareModel The share model containing email and role.
     * @return The share response.
     */
    @PatchMapping(path = { "/ecmr/{ecmrId}/share" })
    @Operation(
            tags = "Anonymous",
            summary = "Share ECMR",
            parameters = {
                    @Parameter(name = "ecmrId", description = "UUID of the ECMR", required = true, schema = @Schema(type = "string", format = "uuid")),
                    @Parameter(name = "userToken", description = "Unique token of the external user", required = true, schema = @Schema(type = "string")),
                    @Parameter(name = "tan", description = "TAN for validation", required = true, schema = @Schema(type = "string"))
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EcmrShareModel.class))),
            responses = {
                    @ApiResponse(description = "Share response",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EcmrShareResponse.class))),
                    @ApiResponse(description = "ECMR not found", responseCode = "404"),
                    @ApiResponse(description = "Not implemented", responseCode = "501"),
                    @ApiResponse(description = "Validation error", responseCode = "400"),
                    @ApiResponse(description = "No permission", responseCode = "403"),
                    @ApiResponse(description = "External user not found", responseCode = "401")
            })
    public ResponseEntity<EcmrShareResponse> shareEcmr(@PathVariable(value = "ecmrId") UUID ecmrId,
            @RequestParam(name = "userToken") @Valid @NotNull String userToken, @RequestParam(name = "tan") @Valid @NotNull String tan,
            @RequestBody @Valid EcmrShareModel ecmrShareModel) {
        try {
            ExternalUser externalUser = this.authenticationService.getExternalUser(ecmrId, userToken, tan);
            return ResponseEntity.ok(
                    this.ecmrShareService.shareEcmr(new InternalOrExternalUser(externalUser), ecmrId, ecmrShareModel.getEmail(),
                            ecmrShareModel.getRole()));
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (NotImplementedException e) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (ExternalUserNotFoundException | ExternalUserInvalidTanException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    /**
     * Downloads the ECMR PDF file.
     *
     * @param ecmrId The UUID of the ECMR.
     * @param userToken Unique token of the external user
     * @param tan The TAN for validation.
     * @return The ECMR PDF file as a StreamingResponseBody.
     */
    @GetMapping("/ecmr/{ecmrId}/pdf")
    @Operation(
            tags = "Anonymous",
            summary = "Download ECMR PDF",
            parameters = {
                    @Parameter(name = "ecmrId", description = "UUID of the ECMR", required = true, schema = @Schema(type = "string", format = "uuid")),
                    @Parameter(name = "userToken", description = "Unique token of the external user", required = true, schema = @Schema(type = "string")),
                    @Parameter(name = "tan", description = "TAN for validation", required = true, schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(description = "ECMR PDF file",
                            content = @Content(mediaType = "application/pdf")),
                    @ApiResponse(description = "No permission", responseCode = "403"),
                    @ApiResponse(description = "PDF creation error", responseCode = "500"),
                    @ApiResponse(description = "ECMR not found", responseCode = "404"),
                    @ApiResponse(description = "External user not found", responseCode = "401")
            })
    public ResponseEntity<StreamingResponseBody> downloadEcmrPdfFile(@PathVariable("ecmrId") UUID ecmrId,
            @RequestParam(name = "userToken") @Valid @NotNull String userToken, @RequestParam(name = "tan") @Valid @NotNull String tan) {
        try {
            ExternalUser externalUser = this.authenticationService.getExternalUser(ecmrId, userToken, tan);
            PdfFile ecmrReport = this.ecmrPdfService.createJasperReportForEcmr(ecmrId, new InternalOrExternalUser(externalUser), true);
            return createPdfResponse(ecmrReport);
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (PdfCreationException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ExternalUserNotFoundException | ExternalUserInvalidTanException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    /**
     * Downloads the ECMR PDF file using a share token.
     *
     * @param id The UUID of the ECMR.
     * @param shareToken The token used for sharing the ECMR.
     * @return The ECMR PDF file as a StreamingResponseBody.
     */
    @GetMapping("/ecmr/{ecmrId}/share-pdf")
    @Operation(
            tags = "Anonymous",
            summary = "Download ECMR PDF with Share Token",
            parameters = {
                    @Parameter(name = "ecmrId", description = "UUID of the ECMR", required = true, schema = @Schema(type = "string", format = "uuid")),
                    @Parameter(name = "shareToken", description = "Share token for accessing the ECMR", required = true, schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(description = "ECMR PDF file",
                            content = @Content(mediaType = "application/pdf")),
                    @ApiResponse(description = "No permission", responseCode = "403"),
                    @ApiResponse(description = "PDF creation error", responseCode = "500"),
                    @ApiResponse(description = "ECMR not found", responseCode = "404")
            })
    public ResponseEntity<StreamingResponseBody> downloadEcmrPdfFileShare(@PathVariable("ecmrId") UUID id,
            @RequestParam @Valid @NotNull String shareToken) {
        try {
            PdfFile ecmrReport = this.ecmrPdfService.createJasperReportForEcmr(id, shareToken, true);
            return createPdfResponse(ecmrReport);
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (PdfCreationException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Retrieves the ECMR roles for an external user.
     *
     * @param ecmrId The UUID of the ECMR.
     * @param userToken Unique token of the external user
     * @param tan The TAN for validation.
     * @return A list of ECMR roles.
     */
    @GetMapping("/ecmr-role")
    @Operation(
            tags = "Anonymous",
            summary = "Get External User ECMR Roles",
            parameters = {
                    @Parameter(name = "ecmrId", description = "UUID of the ECMR", required = true, schema = @Schema(type = "string", format = "uuid")),
                    @Parameter(name = "userToken", description = "Unique token of the external user", required = true, schema = @Schema(type = "string")),
                    @Parameter(name = "tan", description = "TAN for validation", required = true, schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(description = "List of ECMR roles",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EcmrRole.class))),
                    @ApiResponse(description = "ECMR not found", responseCode = "404"),
                    @ApiResponse(description = "External user not found", responseCode = "401")
            })
    public ResponseEntity<List<EcmrRole>> getExternalUserEcmrRoles(@RequestParam(name = "ecmrId") @Valid @NotNull UUID ecmrId,
            @RequestParam(name = "userToken") @Valid @NotNull String userToken, @RequestParam(name = "tan") @NotNull @Valid String tan) {
        try {
            ExternalUser externalUser = authenticationService.getExternalUser(ecmrId, userToken, tan);
            return ResponseEntity.ok(this.ecmrService.getCurrentEcmrRoles(ecmrId, new InternalOrExternalUser(externalUser)));
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ExternalUserNotFoundException | ExternalUserInvalidTanException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    /**
     * Retrieves a specific sealed Document by the eCMR ID
     *
     * @param ecmrId The ID of the eCMR
     * @return The requested eCMR
     */
    @GetMapping(path = { "/sealed-document/{ecmrId}" })
    @Operation(
            tags = "Anonymous",
            summary = "Retrieve Sealed Document without eCMR Model by eCMR ID",
            parameters = {
                    @Parameter(name = "ecmrId", description = "UUID of the eCMR", required = true, schema = @Schema(type = "string", format = "uuid")),
                    @Parameter(name = "userToken", description = "Unique token of the external user", required = true, schema = @Schema(type = "string")),
                    @Parameter(name = "tan", description = "TAN for validation", required = true, schema = @Schema(type = "string"))
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
    public ResponseEntity<SealedDocumentWithoutEcmr> getSealedDocumentWithoutEcmr(@PathVariable(value = "ecmrId") UUID ecmrId,
            @RequestParam(name = "userToken") @Valid @NotNull String userToken, @RequestParam(name = "tan") @NotNull @Valid String tan) {
        try {
            ExternalUser externalUser = authenticationService.getExternalUser(ecmrId, userToken, tan);
            SealedDocumentWithoutEcmr sealedDocumentWithoutEcmr = this.sealedDocumentService.getSealedDocumentWithoutEcmr(ecmrId,
                    new InternalOrExternalUser(externalUser));
            return ResponseEntity.ok(sealedDocumentWithoutEcmr);
        } catch (SealedDocumentNotFoundException e) {
            //Use no ResponseStatusException because we do not want a log, because this will happen regularly and is no error.
            return ResponseEntity.notFound().build();
        } catch (ExternalUserNotFoundException | ExternalUserInvalidTanException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }
}
