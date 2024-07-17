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

import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrCreationService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrUpdateService;
import org.openlogisticsfoundation.ecmr.web.exceptions.AuthenticationException;
import org.openlogisticsfoundation.ecmr.web.mappers.EcmrWebMapper;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EcmrModel>> getAllEcmrs(
        @RequestParam(required = false, defaultValue = "ECMR") EcmrType type,
        @RequestParam(name = "page", defaultValue = "0", required = false) int page,
        @RequestParam(name = "size", defaultValue = "10", required = false) int size,
        @RequestParam(name = "sortBy", defaultValue = "ecmrId", required = false) String sortBy,
        @RequestParam(name = "sortingOrder", defaultValue = "asc", required = false) String sortingOrder) throws AuthenticationException {
        AuthenticatedUser authenticatedUser = this.authenticationService.getAuthenticatedUser();
        List<EcmrModel> ecmrs = this.ecmrService.getAllEcmrs(authenticatedUser, type, page, size, sortBy, sortingOrder);
        return ResponseEntity.ok(ecmrs);
    }

    @GetMapping("/size/{type}")
    @PreAuthorize("isAuthenticated()")
    public Integer getNumberOfEcmrsByType(@PathVariable(value = "type") EcmrType type) {
        return ecmrService.getNumberOfEcmrsByType(type);
    }

    @GetMapping(path = { "{ecmrId}" })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EcmrModel> getEcmr(@PathVariable(value = "ecmrId") UUID ecmrId) {
        try {
            EcmrModel ecmrModel = this.ecmrService.getEcmr(ecmrId);
            return ResponseEntity.ok(ecmrModel);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EcmrModel> createEcmr(@RequestBody EcmrModel ecmrModel) {
        EcmrCommand ecmrCommand = ecmrWebMapper.toCommand(ecmrModel);
        this.ecmrCreationService.createEcmr(ecmrCommand);
        return ResponseEntity.ok(ecmrModel);
    }

    @DeleteMapping("/{ecmrId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void deleteEcmr(@PathVariable(value = "ecmrId") UUID ecmrId)
        throws EcmrNotFoundException {
        ecmrService.deleteEcmr(ecmrId);
    }

    @PatchMapping(path = { "{ecmrId}" })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EcmrModel> changeEcmrType(@PathVariable(value = "ecmrId") UUID ecmrId, @RequestParam EcmrType type) {
        try {
            EcmrModel result = this.ecmrUpdateService.changeType(ecmrId, type);
            return ResponseEntity.ok(result);
        }
        catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
