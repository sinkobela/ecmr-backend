/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openlogisticsfoundation.ecmr.api.model.EcmrConsignment;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.TemplateUser;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.TemplateUserCommand;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrShareService;
import org.openlogisticsfoundation.ecmr.domain.services.TemplateUserService;
import org.openlogisticsfoundation.ecmr.web.mappers.TemplateUserWebMapper;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
public class TemplateUserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EcmrShareService ecmrShareService;

    @MockBean
    private TemplateUserService templateUserService;

    @MockBean
    private TemplateUserWebMapper templateUserWebMapper;

    @MockBean
    public TemplateUserCommand templateUserCommand;

    @MockBean
    private AuthenticationService authenticationService;

    private AuthenticatedUser authenticatedUser;
    private TemplateUser templateUser;

    @BeforeEach
    public void setup() {
        authenticatedUser = Mockito.mock(AuthenticatedUser.class);
        templateUser = new TemplateUser();
        templateUser.setId(1L);
    }

    @Test
    @WithMockUser
    public void testGetAllTemplatesForUser_Success() throws Exception {
        // Arrange
        when(authenticationService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(templateUserService.getTemplatesForCurrentUser(authenticatedUser)).thenReturn(Collections.singletonList(templateUser));
        // Act & Arrange
        mockMvc.perform(get("/template")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testGetTemplate_Success() throws Exception {
        // Arrange
        when(authenticationService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(templateUserService.getTemplateForCurrentUser(authenticatedUser, 1L)).thenReturn(templateUser);
        // Act & Assert
        mockMvc.perform(get("/template/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testCreateTemplate_Success() throws Exception {
        // Arrange
        when(authenticationService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(templateUserService.createTemplate(any(EcmrCommand.class), any(String.class), any(AuthenticatedUser.class)))
            .thenReturn(templateUser);

        EcmrModel ecmrModel = new EcmrModel();
        ecmrModel.setEcmrConsignment(mock(EcmrConsignment.class));
        String name = "Test Template";

        // Act & Assert
        mockMvc.perform(post("/template")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", name)
                .content(new ObjectMapper().writeValueAsString(ecmrModel)))
            .andExpect(status().isOk());

        verify(templateUserService, times(1)).createTemplate(any(EcmrCommand.class), eq(name), any(AuthenticatedUser.class));
    }

    @Test
    @WithMockUser
    public void testUpdateTemplate_Success() throws Exception {
        // Arrange
        TemplateUser templateUser = new TemplateUser();
        templateUser.setId(1L);

        when(templateUserWebMapper.toCommand(any())).thenReturn(templateUserCommand);
        when(templateUserService.updateTemplate(templateUserCommand)).thenReturn(templateUser);

        // Act & Assert
        mockMvc.perform(patch("/template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(templateUser)))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(templateUser.getId()));

        verify(templateUserService, times(1)).updateTemplate(templateUserCommand);
    }

    @Test
    @WithMockUser
    public void testShareTemplate_Success() throws Exception {
        // Arrange
        Long templateId = 1L;
        List<Long> userIDs = Arrays.asList(2L, 3L);

        doNothing().when(templateUserService).shareTemplate(templateId, userIDs);

        // Act & Assert
        mockMvc.perform(post("/template/share/{id}", templateId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userIDs)))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        verify(templateUserService, times(1)).shareTemplate(templateId, userIDs);
    }

    @Test
    @WithMockUser
    public void testDeleteTemplate_Success() throws Exception {
        // Arrange
        Long templateId = 1L;
        doNothing().when(templateUserService).deleteTemplate(templateId);

        // Act & Assert
        mockMvc.perform(delete("/template/{id}", templateId))
            .andExpect(status().isOk());

        verify(templateUserService, times(1)).deleteTemplate(templateId);
    }
}
