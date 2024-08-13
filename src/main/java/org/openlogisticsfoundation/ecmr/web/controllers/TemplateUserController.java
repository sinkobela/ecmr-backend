/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import java.util.List;

import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.domain.exceptions.TemplateUserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.UserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.TemplateUser;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.TemplateUserCommand;
import org.openlogisticsfoundation.ecmr.domain.services.TemplateUserService;
import org.openlogisticsfoundation.ecmr.web.exceptions.AuthenticationException;
import org.openlogisticsfoundation.ecmr.web.mappers.EcmrWebMapper;
import org.openlogisticsfoundation.ecmr.web.mappers.TemplateUserWebMapper;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/template")
@RequiredArgsConstructor
public class TemplateUserController {

    private final EcmrWebMapper ecmrWebMapper;
    private final TemplateUserWebMapper templateUserWebMapper;
    private final TemplateUserService templateUserService;
    private final AuthenticationService authenticationService;

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TemplateUser>> getAllTemplatesForUser() {
        List<TemplateUser> templates = this.templateUserService.getTemplatesForCurrentUser();
        return ResponseEntity.ok(templates);
    }

    @GetMapping(path = { "{id}" })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TemplateUser> getTemplate(@PathVariable(value = "id") Long id) {
        try {
            return ResponseEntity.ok(templateUserService.getTemplateForCurrentUser(id));
        } catch (TemplateUserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TemplateUser> createTemplate(@RequestBody EcmrModel ecmrModel, @RequestParam String name) {
        EcmrCommand ecmrCommand = ecmrWebMapper.toCommand(ecmrModel);
        try {
            AuthenticatedUser authenticatedUser = this.authenticationService.getAuthenticatedUser();
            return ResponseEntity.ok(this.templateUserService.createTemplate(ecmrCommand, name, authenticatedUser));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @PatchMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TemplateUser> updateTemplate(@RequestBody TemplateUser templateUser) {
        try {
            TemplateUserCommand templateUserCommand = templateUserWebMapper.toCommand(templateUser);
            return ResponseEntity.ok(this.templateUserService.updateTemplate(templateUserCommand));
        } catch (TemplateUserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PostMapping(path = { "/share/{id}" })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TemplateUser> shareTemplate(@PathVariable(value = "id") Long id, @RequestBody List<Long> userIDs)
            throws TemplateUserNotFoundException {
        try {
            AuthenticatedUser authenticatedUser = this.authenticationService.getAuthenticatedUser();
            this.templateUserService.shareTemplate(id, userIDs, authenticatedUser);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AuthenticationException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
        return ResponseEntity.ok(new TemplateUser());
    }

    @DeleteMapping(path = { "/{id}" })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteTemplate(@PathVariable(value = "id") Long id) {
        try {
            templateUserService.deleteTemplate(id);
            return ResponseEntity.ok().build();
        } catch (TemplateUserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

    }
}
