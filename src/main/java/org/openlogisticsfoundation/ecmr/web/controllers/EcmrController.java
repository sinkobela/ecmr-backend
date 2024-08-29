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
import org.openlogisticsfoundation.ecmr.api.model.signature.Signature;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.PdfCreationException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.SignatureAlreadyPresentException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.UserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrShareResponse;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.SignatureType;
import org.openlogisticsfoundation.ecmr.domain.models.SortingField;
import org.openlogisticsfoundation.ecmr.domain.models.SortingOrder;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrCreationService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrPdfService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrShareService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrSignService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrUpdateService;
import org.openlogisticsfoundation.ecmr.web.exceptions.AuthenticationException;
import org.openlogisticsfoundation.ecmr.web.mappers.EcmrWebMapper;
import org.openlogisticsfoundation.ecmr.web.models.EcmrPageModel;
import org.openlogisticsfoundation.ecmr.web.models.EcmrShareModel;
import org.openlogisticsfoundation.ecmr.web.models.FilterRequestModel;
import org.openlogisticsfoundation.ecmr.web.models.SignModel;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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
    private final EcmrPdfService ecmrPdfService;
    private final EcmrWebMapper ecmrWebMapper;
    private final AuthenticationService authenticationService;
    private final EcmrShareService ecmrShareService;
    private final EcmrSignService ecmrSignService;

    /**
     * Retrieves a paginated and sorted list of {@link EcmrModel}.
     *
     * @param type         The type of eCMRs.
     * @param page         The page number, that should be used to select the eCMRs.
     * @param size         The size of the paginated list, that determines how many objects should be returned.
     * @param sortBy       The column name used for sorting the result.
     * @param sortingOrder The sorting order used for sorting the result.
     * @return A paginated and sorted list of {@link EcmrModel}.
     */
    @PostMapping("/my-ecmrs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EcmrPageModel> getMyEcmrs(@RequestParam(required = false, defaultValue = "ECMR") EcmrType type,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            @RequestParam(name = "sortBy", defaultValue = "referenceId", required = false) SortingField sortBy,
            @RequestParam(name = "sortingOrder", defaultValue = "ASC", required = false) SortingOrder sortingOrder,
            @RequestBody FilterRequestModel filterRequestModel
    )
            throws AuthenticationException {
        AuthenticatedUser authenticatedUser = this.authenticationService.getAuthenticatedUser();
        EcmrPageModel pageModel = this.ecmrService.getEcmrsForUser(authenticatedUser, type, page, size, sortBy, sortingOrder,
                ecmrWebMapper.map(filterRequestModel));
        return ResponseEntity.ok(pageModel);
    }

    @GetMapping(path = { "{ecmrId}" })
    @PreAuthorize("isAuthenticated()")
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

    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EcmrModel> createEcmr(@RequestBody EcmrModel ecmrModel, @RequestParam(name = "groupId") List<Long> groupIds) {
        EcmrCommand ecmrCommand = ecmrWebMapper.toCommand(ecmrModel);
        try {
            AuthenticatedUser authenticatedUser = this.authenticationService.getAuthenticatedUser();
            this.ecmrCreationService.createEcmr(ecmrCommand, authenticatedUser, groupIds);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
        return ResponseEntity.ok(ecmrModel);
    }

    @DeleteMapping("/{ecmrId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
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

    @PatchMapping(path = { "{ecmrId}/archive" })
    @PreAuthorize("isAuthenticated()")
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

    @PatchMapping(path = { "{ecmrId}/reactivate" })
    @PreAuthorize("isAuthenticated()")
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

    @PatchMapping(path = { "{ecmrId}/share" })
    @PreAuthorize("isAuthenticated()")
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

    @GetMapping(path = { "{ecmrId}/import" })
    @PreAuthorize("isAuthenticated()")
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

    @GetMapping("/{ecmrId}/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StreamingResponseBody> downloadEcmrPdfFile(@PathVariable("ecmrId") UUID id) {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            byte[] ecmrReportData = this.ecmrPdfService.createJasperReportForEcmr(id, new InternalOrExternalUser(authenticatedUser.getUser()));
            return createPdfResponse(ecmrReportData);
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

    @PutMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EcmrModel> updateEcmr(@RequestBody EcmrModel ecmrModel) {
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

    @PostMapping("/{ecmrId}/sign-on-glass")
    @PreAuthorize("isAuthenticated()")
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

    @GetMapping("{ecmrId}/share-token")
    @PreAuthorize("isAuthenticated()")
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

    @GetMapping("/{ecmrId}/role")
    @PreAuthorize("isAuthenticated()")
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
