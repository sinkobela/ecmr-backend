/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */


package org.openlogisticsfoundation.ecmr.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.signature.Signature;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ExternalUserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.PdfCreationException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.SignatureAlreadyPresentException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrShareResponse;
import org.openlogisticsfoundation.ecmr.domain.models.ExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.SignatureType;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ExternalUserRegistrationCommand;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrPdfService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrShareService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrSignService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrUpdateService;
import org.openlogisticsfoundation.ecmr.domain.services.ExternalUserService;
import org.openlogisticsfoundation.ecmr.domain.services.tan.MessageProviderException;
import org.openlogisticsfoundation.ecmr.web.mappers.EcmrWebMapper;
import org.openlogisticsfoundation.ecmr.web.mappers.ExternalUserWebMapper;
import org.openlogisticsfoundation.ecmr.web.models.EcmrShareModel;
import org.openlogisticsfoundation.ecmr.web.models.ExternalUserRegistrationModel;
import org.openlogisticsfoundation.ecmr.web.models.SignModel;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.http.HttpHeaders;
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
    private final EcmrSignService ecmrSignService;
    private final EcmrPdfService ecmrPdfService;

    @GetMapping("/is-tan-valid")
    public ResponseEntity<Boolean> isTanValid(@RequestParam(name = "ecmrId") @Valid @NotNull UUID ecmrId,
            @RequestParam(name = "tan") @NotNull @Valid String tan) {
        try {
            return ResponseEntity.ok(this.externalUserService.isTanValid(ecmrId, tan));
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/registration")
    public void registerExternalUser(@Valid @RequestBody ExternalUserRegistrationModel externalUserRegistrationModel) {
        try {
            ExternalUserRegistrationCommand command = externalUserWebMapper.map(externalUserRegistrationModel);
            this.ecmrShareService.registerExternalUser(command);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (MessageProviderException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping(path = { "/ecmr/{ecmrId}" })
    public ResponseEntity<EcmrModel> getEcmrWithTan(@PathVariable(value = "ecmrId") UUID ecmrId,
            @RequestParam(name = "tan") @Valid @NotNull String tan) {
        try {
            ExternalUser externalUser = authenticationService.getExternalUser(ecmrId, tan);
            EcmrModel ecmrModel = this.ecmrService.getEcmr(ecmrId, new InternalOrExternalUser(externalUser));
            return ResponseEntity.ok(ecmrModel);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (ExternalUserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @PutMapping("/ecmr")
    public ResponseEntity<EcmrModel> updateEcmr(@RequestParam(name = "tan") @Valid @NotNull String tan,
            @RequestBody EcmrModel ecmrModel) {
        try {
            UUID ecmrId = UUID.fromString(ecmrModel.getEcmrId());
            ExternalUser externalUser = this.authenticationService.getExternalUser(ecmrId, tan);
            EcmrCommand ecmrCommand = ecmrWebMapper.toCommand(ecmrModel);
            EcmrModel result = this.ecmrUpdateService.updateEcmr(ecmrCommand, ecmrId, new InternalOrExternalUser(externalUser));
            return ResponseEntity.ok(result);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (ExternalUserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @PostMapping("/ecmr/{ecmrId}/sign-on-glass")
    public ResponseEntity<Signature> signOnGlass(@PathVariable(value = "ecmrId") UUID ecmrId,
            @RequestParam(name = "tan") @Valid @NotNull String tan, @RequestBody @Valid @NotNull SignModel signModel) {
        try {
            ExternalUser externalUser = this.authenticationService.getExternalUser(ecmrId, tan);
            return ResponseEntity.ok(this.ecmrSignService.signEcmr(new InternalOrExternalUser(externalUser), ecmrId,
                    ecmrWebMapper.map(signModel), SignatureType.SignOnGlass));
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ValidationException | SignatureAlreadyPresentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (ExternalUserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @GetMapping("/ecmr/{ecmrId}/share-token")
    public ResponseEntity<String> getShareToken(@PathVariable(value = "ecmrId") UUID ecmrId, @RequestParam(name = "tan") @Valid @NotNull String tan,
            @RequestParam(name = "ecmrRole") @Valid @NotNull EcmrRole ecmrRole) {
        try {
            ExternalUser externalUser = this.authenticationService.getExternalUser(ecmrId, tan);
            return ResponseEntity.ok(this.ecmrShareService.getShareToken(ecmrId, ecmrRole, new InternalOrExternalUser(externalUser)));
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (ExternalUserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @PatchMapping(path = { "/ecmr/{ecmrId}/share" })
    public ResponseEntity<EcmrShareResponse> shareEcmr(@PathVariable(value = "ecmrId") UUID ecmrId,
            @RequestParam(name = "tan") @Valid @NotNull String tan, @RequestBody @Valid EcmrShareModel ecmrShareModel) {
        try {
            ExternalUser externalUser = this.authenticationService.getExternalUser(ecmrId, tan);
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
        } catch (ExternalUserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @GetMapping("/ecmr/{ecmrId}/pdf")
    public ResponseEntity<StreamingResponseBody> downloadEcmrPdfFile(@PathVariable("ecmrId") UUID id,
            @RequestParam @Valid @NotNull String shareToken) {
        try {
            byte[] ecmrReportData = this.ecmrPdfService.createJasperReportForEcmr(id, shareToken);
            StreamingResponseBody streamingResponseBody = outputStream -> {
                try (InputStream inputStream = new ByteArrayInputStream(ecmrReportData)) {
                    inputStream.transferTo(outputStream);
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
                }
            };
            return ResponseEntity.ok().contentLength(ecmrReportData.length).contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ecmr-report.pdf\"")
                    .body(streamingResponseBody);
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (PdfCreationException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/ecmr-role")
    public ResponseEntity<List<EcmrRole>> getExternalUserEcmrRoles(@RequestParam(name = "ecmrId") @Valid @NotNull UUID ecmrId,
            @RequestParam(name = "tan") @NotNull @Valid String tan) {
        try {
            ExternalUser externalUser = authenticationService.getExternalUser(ecmrId, tan);
            return ResponseEntity.ok(this.ecmrService.getCurrentEcmrRoles(ecmrId, new InternalOrExternalUser(externalUser)));
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ExternalUserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
}
