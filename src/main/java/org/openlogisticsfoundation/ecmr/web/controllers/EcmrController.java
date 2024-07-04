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
import org.openlogisticsfoundation.ecmr.domain.services.EcmrUpdateService;
import org.openlogisticsfoundation.ecmr.web.mappers.EcmrWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PatchMapping(path = { "{ecmrId}" })
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
