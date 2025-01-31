/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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

    /**
     * Retrieves all templates for the authenticated user
     *
     * @return A list of templates for the authenticated user
     */
    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "Template",
        summary = "Retrieve All Templates",
        responses = {
            @ApiResponse(description = "List of templates for the authenticated user",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TemplateUser.class))),
            @ApiResponse(description = "Unauthorized access", responseCode = "401")
        })
    public ResponseEntity<List<TemplateUser>> getAllTemplatesForUser() throws AuthenticationException {
        AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
        List<TemplateUser> templates = this.templateUserService.getTemplatesForCurrentUser(authenticatedUser);
        return ResponseEntity.ok(templates);
    }

    /**
     * Retrieves a specific template by ID for the authenticated user
     *
     * @param id The ID of the template
     * @return The requested template
     */
    @GetMapping(path = { "{id}" })
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "Template",
        summary = "Retrieve Template by ID",
        parameters = {
            @Parameter(name = "id", description = "UUID of the template", required = true, schema = @Schema(type = "integer"))
        },
        responses = {
            @ApiResponse(description = "The requested template",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TemplateUser.class))),
            @ApiResponse(description = "Template not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401")
        })
    public ResponseEntity<TemplateUser> getTemplate(@PathVariable(value = "id") Long id) throws AuthenticationException {
        AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
        try {
            return ResponseEntity.ok(templateUserService.getTemplateForCurrentUser(authenticatedUser, id));
        } catch (TemplateUserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    /**
     * Creates a new template for the authenticated user
     *
     * @param ecmrModel The eCMR model data
     * @param name      The name of the template
     * @return The created template
     */
    @PostMapping()
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "Template",
        summary = "Create a New Template",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = EcmrModel.class))),
        responses = {
            @ApiResponse(description = "The created template",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TemplateUser.class))),
            @ApiResponse(description = "User not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401")
        })
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

    /**
     * Updates an existing template
     *
     * @param templateUser The updated template data
     * @return The updated template
     */
    @PatchMapping()
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "Template",
        summary = "Update Existing Template",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = TemplateUser.class))),
        responses = {
            @ApiResponse(description = "The updated template",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TemplateUser.class))),
            @ApiResponse(description = "Template not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401")
        })
    public ResponseEntity<TemplateUser> updateTemplate(@RequestBody TemplateUser templateUser) {
        try {
            TemplateUserCommand templateUserCommand = templateUserWebMapper.toCommand(templateUser);
            return ResponseEntity.ok(this.templateUserService.updateTemplate(templateUserCommand));
        } catch (TemplateUserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    /**
     * Shares a template with specified user IDs
     *
     * @param id      The ID of the template to share
     * @param userIDs The list of user IDs to share with
     * @return The shared template
     */
    @PostMapping(path = { "/share/{id}" })
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "Template",
        summary = "Share Template",
        parameters = {
            @Parameter(name = "id", description = "UUID of the template to share", required = true, schema = @Schema(type = "integer"))
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(type = "integer"))),
        responses = {
            @ApiResponse(description = "Successfully shared template",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TemplateUser.class))),
            @ApiResponse(description = "Template not found", responseCode = "404"),
            @ApiResponse(description = "User not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401")
        })
    public ResponseEntity<TemplateUser> shareTemplate(@PathVariable(value = "id") Long id, @RequestBody List<Long> userIDs)
            throws TemplateUserNotFoundException, AuthenticationException {
        try {
            AuthenticatedUser authenticatedUser = authenticationService.getAuthenticatedUser();
            this.templateUserService.shareTemplate(authenticatedUser, id, userIDs);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (NoPermissionException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
        return ResponseEntity.ok(new TemplateUser());
    }

    /**
     * Deletes a template by ID
     *
     * @param id The ID of the template to delete
     */
    @DeleteMapping(path = { "/{id}" })
    @PreAuthorize("isAuthenticated()")
    @Operation(
        tags = "Template",
        summary = "Delete Template",
        parameters = {
            @Parameter(name = "id", description = "UUID of the template to delete", required = true, schema = @Schema(type = "integer"))
        },
        responses = {
            @ApiResponse(description = "Template deleted successfully", responseCode = "204"),
            @ApiResponse(description = "Template not found", responseCode = "404"),
            @ApiResponse(description = "Unauthorized access", responseCode = "401")
        })
    public ResponseEntity<?> deleteTemplate(@PathVariable(value = "id") Long id) {
        try {
            templateUserService.deleteTemplate(id);
            return ResponseEntity.ok().build();
        } catch (TemplateUserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }
}
