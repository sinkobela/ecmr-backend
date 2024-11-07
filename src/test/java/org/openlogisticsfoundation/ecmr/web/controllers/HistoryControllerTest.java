/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openlogisticsfoundation.ecmr.domain.models.*;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrShareService;
import org.openlogisticsfoundation.ecmr.domain.services.HistoryLogService;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
public class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EcmrShareService ecmrShareService;

    @MockBean
    private AuthenticationService authenticationServiceMock;

    @MockBean
    private HistoryLogService historyLogServiceMock;


    private User mockUser;

    @BeforeEach
    public void setUp() {

        mockUser = new User(
            12345, // id
            "Max", // firstName
            "Mustermann", // lastName
            CountryCode.DE, // country
            "max.mustermann@example.com", // email
            "+49123456789", // phone
            UserRole.Admin, // role
            null, // defaultGroupId
            false, // deactivated
            false // technical

        );
    }

    @Test
    @WithMockUser
    public void testGetHistoryLogs_Success() throws Exception {
        // Arrange
        UUID ecmrId = UUID.randomUUID();
        List<HistoryLog> historyLogs = new ArrayList<>();
        historyLogs.add(new HistoryLog());

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(this.mockUser);
        when(authenticationServiceMock.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(historyLogServiceMock.getLogs(eq(ecmrId), any(InternalOrExternalUser.class))).thenReturn(historyLogs);
        // Act & Assert
        mockMvc.perform(get("/history/{ecmrId}", ecmrId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
