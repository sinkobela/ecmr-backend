/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openlogisticsfoundation.ecmr.api.model.SealedDocument;
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrShareService;
import org.openlogisticsfoundation.ecmr.web.exceptions.AuthenticationException;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext
public class ExternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EcmrShareService ecmrShareService;

    @MockBean
    private SealedDocument sealedDocument;

    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private AuthenticatedUser authenticatedUser;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UUID ecmrId = UUID.randomUUID();
    private final String shareToken = "share";
    private final String url = "test-url";
    private final String groupId = "1";

    @BeforeEach
    public void setUp() throws Exception {
        when(authenticationService.getAuthenticatedUser(true)).thenReturn(authenticatedUser);
    }

    // EXPORT

    @Test
    public void exportEcmr_successful() throws Exception {
        // Arrange
        when(ecmrShareService.exportEcmrToExternal(ecmrId, shareToken)).thenReturn(sealedDocument);

        // Act & Assert
        mockMvc.perform(get("/external/ecmr/{ecmrId}/export", ecmrId)
                .param("shareToken", shareToken))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(sealedDocument)));

        verify(ecmrShareService, times(1)).exportEcmrToExternal(ecmrId, shareToken);
    }

    @Test
    public void exportEcmr_invalidShareToken() throws Exception {
        // Arrange
        when(ecmrShareService.exportEcmrToExternal(ecmrId, shareToken)).thenThrow(ValidationException.class);

        // Act & Assert
        mockMvc.perform(get("/external/ecmr/{ecmrId}/export", ecmrId)
                .param("shareToken", shareToken))
            .andExpect(status().isForbidden());

        verify(ecmrShareService, times(1)).exportEcmrToExternal(ecmrId, shareToken);
    }

    @Test
    public void exportEcmr_invalidEcmrId() throws Exception {
        // Arrange
        when(ecmrShareService.exportEcmrToExternal(ecmrId, shareToken)).thenThrow(NoSuchElementException.class);

        // Act & Assert
        mockMvc.perform(get("/external/ecmr/{ecmrId}/export", ecmrId)
                .param("shareToken", shareToken))
            .andExpect(status().isNotFound());

        verify(ecmrShareService, times(1)).exportEcmrToExternal(ecmrId, shareToken);
    }

    // IMPORT

    @Test
    @WithMockUser
    public void importEcmr_successful() throws Exception {
        // Act
        mockMvc.perform(post("/external/ecmr/import")
                .param("ecmrId", ecmrId.toString())
                .param("shareToken", shareToken)
                .param("url", url)
                .param("groupId", groupId))
            .andExpect(status().isOk());

        verify(ecmrShareService, times(1)).importEcmrFromExternal(url, ecmrId, shareToken, List.of(Long.valueOf(groupId)), authenticatedUser);
    }

    @Test
    @WithMockUser
    public void importEcmr_invalidAuthentication() throws Exception {
        // Arrange
        when(authenticationService.getAuthenticatedUser(true)).thenThrow(AuthenticationException.class);

        // Act
        mockMvc.perform(post("/external/ecmr/import")
                .param("ecmrId", ecmrId.toString())
                .param("shareToken", shareToken)
                .param("url", url)
                .param("groupId", groupId))
            .andExpect(status().isUnauthorized());

        verify(ecmrShareService, times(0)).importEcmrFromExternal(url, ecmrId, shareToken, List.of(Long.valueOf(groupId)), authenticatedUser);
    }

    @Test
    @WithMockUser
    public void importEcmr_invalidGroupIds() throws Exception {
        // Arrange
        doThrow(NoPermissionException.class)
            .when(ecmrShareService).importEcmrFromExternal(url, ecmrId, shareToken, List.of(Long.valueOf(groupId)), authenticatedUser);

        // Act
        mockMvc.perform(post("/external/ecmr/import")
                .param("ecmrId", ecmrId.toString())
                .param("shareToken", shareToken)
                .param("url", url)
                .param("groupId", groupId))
            .andExpect(status().isForbidden());

        verify(ecmrShareService, times(1)).importEcmrFromExternal(url, ecmrId, shareToken, List.of(Long.valueOf(groupId)), authenticatedUser);
    }

    @Test
    @WithMockUser
    public void importEcmr_invalidSeal() throws Exception {
        // Arrange
        doThrow(InvalidInputException.class)
            .when(ecmrShareService).importEcmrFromExternal(url, ecmrId, shareToken, List.of(Long.valueOf(groupId)), authenticatedUser);

        // Act
        mockMvc.perform(post("/external/ecmr/import")
                .param("ecmrId", ecmrId.toString())
                .param("shareToken", shareToken)
                .param("url", url)
                .param("groupId", groupId))
            .andExpect(status().isBadRequest());

        verify(ecmrShareService, times(1)).importEcmrFromExternal(url, ecmrId, shareToken, List.of(Long.valueOf(groupId)), authenticatedUser);
    }

}
