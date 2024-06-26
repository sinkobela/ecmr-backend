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
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrCreationService;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrService;
import org.openlogisticsfoundation.ecmr.web.mappers.EcmrWebMapper;
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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ecmr")
@RequiredArgsConstructor
public class EcmrController {

    private final EcmrService ecmrService;
    private final EcmrCreationService ecmrCreationService;
    private final EcmrWebMapper ecmrWebMapper;

    @GetMapping()
    public ResponseEntity<List<EcmrModel>> getAllEcmrs(@RequestParam(required = false, defaultValue = "ECMR") EcmrType type ) {
        List<EcmrModel> ecmrs = this.ecmrService.getAllEcmrs(type);
        return ResponseEntity.ok(ecmrs);
    }

    @GetMapping(path = { "{ecmrId}" })
    public ResponseEntity<EcmrModel> getEcmr(@PathVariable(value = "ecmrId") UUID ecmrId) {
        try {
            EcmrModel ecmrModel = this.ecmrService.getEcmr(ecmrId);
            return ResponseEntity.ok(ecmrModel);
        } catch (EcmrNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping()
    public ResponseEntity<EcmrModel> createEcmr(@RequestBody EcmrModel ecmrModel) {
        EcmrCommand ecmrCommand = ecmrWebMapper.toCommand(ecmrModel);
        this.ecmrCreationService.createEcmr(ecmrCommand);
        return ResponseEntity.ok(new EcmrModel());
    }
}
