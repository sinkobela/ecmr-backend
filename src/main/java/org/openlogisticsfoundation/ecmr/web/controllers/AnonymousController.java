/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */


package org.openlogisticsfoundation.ecmr.web.controllers;

import java.util.UUID;

import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ExternalUserRegistrationCommand;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrTanService;
import org.openlogisticsfoundation.ecmr.domain.services.ExternalUserService;
import org.openlogisticsfoundation.ecmr.domain.services.tan.MessageProviderException;
import org.openlogisticsfoundation.ecmr.web.mappers.ExternalUserWebMapper;
import org.openlogisticsfoundation.ecmr.web.models.ExternalUserRegistrationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/anonymous")
@RequiredArgsConstructor
public class AnonymousController {
    private final ExternalUserService externalUserService;
    private final ExternalUserWebMapper externalUserWebMapper;
    private final EcmrTanService ecmrTanService;
    private final EcmrService ecmrService;

    @GetMapping("/is-tan-valid")
    public ResponseEntity<Boolean> isTanValid(@RequestParam(name = "ecmrId") @Valid @NotNull UUID ecmrId, @RequestParam(name = "tan") @NotNull @Valid String tan) {
        try {
            return ResponseEntity.ok(this.ecmrTanService.isTanValid(ecmrId, tan));
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping(path = { "/ecmr/{ecmrId}" })
    public ResponseEntity<EcmrModel> getEcmrWithTan(@PathVariable(value = "ecmrId") UUID ecmrId, @RequestParam(name = "tan", required = true) @NotNull String tan) {
        try {
            EcmrModel ecmrModel = this.ecmrService.getEcmr(ecmrId);
            return ResponseEntity.ok(ecmrModel);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/registration")
    public void registerExternalUser(@Valid @RequestBody ExternalUserRegistrationModel externalUserRegistrationModel) {
        try {
            ExternalUserRegistrationCommand command = externalUserWebMapper.map(externalUserRegistrationModel);
            this.externalUserService.registerExternalUser(command);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (MessageProviderException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
