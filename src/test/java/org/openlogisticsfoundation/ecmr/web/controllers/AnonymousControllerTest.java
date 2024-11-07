/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openlogisticsfoundation.ecmr.api.model.EcmrConsignment;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.signature.Signature;
import org.openlogisticsfoundation.ecmr.domain.models.*;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ExternalUserRegistrationCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.SignCommand;
import org.openlogisticsfoundation.ecmr.domain.services.*;
import org.openlogisticsfoundation.ecmr.web.mappers.EcmrWebMapper;
import org.openlogisticsfoundation.ecmr.web.mappers.ExternalUserWebMapper;
import org.openlogisticsfoundation.ecmr.web.models.EcmrShareModel;
import org.openlogisticsfoundation.ecmr.web.models.ExternalUserRegistrationModel;
import org.openlogisticsfoundation.ecmr.web.models.SignModel;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

/*
 * Test class for AnonymousController
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext
public class AnonymousControllerTest {

    @MockBean
    private EcmrShareService ecmrShareService;

    @MockBean
    private ExternalUserWebMapper externalUserWebMapper;

    @MockBean
    private ExternalUserService externalUserService;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private EcmrService ecmrService;

    @MockBean
    private EcmrUpdateService ecmrUpdateService;

    @MockBean
    private EcmrSignService ecmrSignService;

    @MockBean
    private EcmrWebMapper ecmrWebMapper;

    @Autowired
    private MockMvc mockMvc;

    private ExternalUserRegistrationModel registrationModel;

    @BeforeEach
    void setUp() {
        registrationModel = new ExternalUserRegistrationModel(UUID.randomUUID(), "valid_share_token", "John", "Doe", "Example Company", "john.doe@example.com", "123456789");
    }

    @Test
    public void testIsTanValid_Success() throws Exception {
        // Arrange
        UUID ecmrId = UUID.randomUUID();
        String tan = "valid-tan";
        when(externalUserService.isTanValid(ecmrId, tan)).thenReturn(true);

        // Act
        mockMvc.perform(get("/anonymous/is-tan-valid").param("ecmrId", ecmrId.toString()).param("tan", tan)).andExpect(status().isOk());

        // Assert
        verify(externalUserService, times(1)).isTanValid(ecmrId, tan);
    }

    @Test
    public void testRegisterExternalUser_Success() throws Exception {
        // Arrange
        ExternalUserRegistrationCommand command = new ExternalUserRegistrationCommand(registrationModel.getEcmrId(), registrationModel.getShareToken(), registrationModel.getFirstName(), registrationModel.getLastName(), registrationModel.getCompany(), registrationModel.getEmail(), registrationModel.getPhone());

        when(externalUserWebMapper.map(any())).thenReturn(command);
        doNothing().when(ecmrShareService).registerExternalUser(command);

        String jsonRequest = new ObjectMapper().writeValueAsString(registrationModel);

        // Act
        mockMvc.perform(post("/anonymous/registration").characterEncoding("UTF-8").contentType(MediaType.APPLICATION_JSON).content(jsonRequest)).andExpect(status().isOk());

        // Assert
        verify(externalUserWebMapper, times(1)).map(any(ExternalUserRegistrationModel.class));
        verify(ecmrShareService, times(1)).registerExternalUser(any(ExternalUserRegistrationCommand.class));
    }

    @Test
    public void testGetEcmrWith_Success() throws Exception {
            // Arrange
        UUID ecmrId = UUID.randomUUID();
        String tan = "valid-tan";
        ExternalUser externalUser = new ExternalUser(1L, "John", "Doe", "Example Company", "john.doe@example.com", "123456789", tan, Instant.now().plusSeconds(3600));

        EcmrModel ecmrModel = new EcmrModel();

        when(authenticationService.getExternalUser(eq(ecmrId), eq(tan))).thenReturn(externalUser);
        when(ecmrService.getEcmr(eq(ecmrId), any(InternalOrExternalUser.class))).thenReturn(ecmrModel);

        // Act
        mockMvc.perform(get("/anonymous/ecmr/{ecmrId}", ecmrId).param("tan", tan).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getExternalUser(eq(ecmrId), eq(tan));
        verify(ecmrService, times(1)).getEcmr(eq(ecmrId), any(InternalOrExternalUser.class));
    }

    @Test
    public void testUpdateEcmr_Success() throws Exception {
        // Arrange

        String tan = "valid-tan";
        EcmrConsignment ecmrConsignment = new EcmrConsignment();
        EcmrModel ecmrModel = new EcmrModel();
        ecmrModel.setEcmrId("7f965664-da65-41c4-b155-2481f77678ef");
        ecmrModel.setEcmrConsignment(ecmrConsignment);


        ExternalUser externalUser = new ExternalUser(1L, "John", "Doe", "Example Company", "john.doe@example.com", "123456789", tan, Instant.now().plusSeconds(3600));

        EcmrCommand ecmrCommand = mock(EcmrCommand.class);

        when(authenticationService.getExternalUser(eq(UUID.fromString(ecmrModel.getEcmrId())), eq(tan))).thenReturn(externalUser);
        when(ecmrWebMapper.toCommand(ecmrModel)).thenReturn(ecmrCommand);
        when(ecmrUpdateService.updateEcmr(eq(ecmrCommand), eq(UUID.fromString(ecmrModel.getEcmrId())), any(InternalOrExternalUser.class))).thenReturn(ecmrModel);

        String jsonRequest = new ObjectMapper().writeValueAsString(ecmrModel);

        // Act
        mockMvc.perform(put("/anonymous/ecmr").characterEncoding("UTF-8").param("tan", tan).contentType(MediaType.APPLICATION_JSON).content(jsonRequest)).andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getExternalUser(eq(UUID.fromString(ecmrModel.getEcmrId())), eq(tan));
        verify(ecmrWebMapper, times(1)).toCommand(ecmrModel);
        verify(ecmrUpdateService, times(1)).updateEcmr(eq(ecmrCommand), eq(UUID.fromString(ecmrModel.getEcmrId())), any(InternalOrExternalUser.class));
    }

    @Test
    public void testSignOnGlass_Success() throws Exception {
        // Arrange
        UUID ecmrId = UUID.randomUUID();
        String tan = "valid-tan";
        SignModel signModel = new SignModel(Signer.Consignee, "signatureData", "Sample City");

        ExternalUser externalUser = new ExternalUser(1L, "John", "Doe", "Example Company", "john.doe@example.com", "123456789", tan, Instant.now().plusSeconds(3600));

        SignCommand signCommand = new SignCommand(Signer.Sender, "signatureData", "Sample City");

        Signature signature = new Signature();

        when(authenticationService.getExternalUser(eq(ecmrId), eq(tan))).thenReturn(externalUser);
        when(ecmrWebMapper.map(any(SignModel.class))).thenReturn(signCommand);
        when(ecmrSignService.signEcmr(any(InternalOrExternalUser.class), eq(ecmrId), eq(signCommand), eq(SignatureType.SignOnGlass))).thenReturn(signature);

        String jsonRequest = new ObjectMapper().writeValueAsString(signModel);

        // Act
        mockMvc.perform(post("/anonymous/ecmr/{ecmrId}/sign-on-glass", ecmrId).characterEncoding("UTF-8").param("tan", tan).contentType(MediaType.APPLICATION_JSON).content(jsonRequest)).andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getExternalUser(eq(ecmrId), eq(tan));
        verify(ecmrWebMapper, times(1)).map(any(SignModel.class));
        verify(ecmrSignService, times(1)).signEcmr(any(InternalOrExternalUser.class), eq(ecmrId), eq(signCommand), eq(SignatureType.SignOnGlass));
    }

    @Test
    public void testGetShareToken_Success() throws Exception {
        // Arrange
        UUID ecmrId = UUID.randomUUID();
        String tan = "valid-tan";
        EcmrRole role = EcmrRole.Consignee;
        ExternalUser externalUser = new ExternalUser(1L, "John", "Doe", "Example Company", "john.doe@example.com", "123456789", tan, Instant.now().plusSeconds(3600));

        String shareToken = "share-token";

        when(authenticationService.getExternalUser(eq(ecmrId), eq(tan))).thenReturn(externalUser);
        when(ecmrShareService.getShareToken(eq(ecmrId), eq(role), any(InternalOrExternalUser.class))).thenReturn(shareToken);
        // Act
        mockMvc.perform(get("/anonymous/ecmr/{ecmrId}/share-token", ecmrId).param("tan", tan).param("ecmrRole", role.name()).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getExternalUser(eq(ecmrId), eq(tan));
        verify(ecmrShareService, times(1)).getShareToken(eq(ecmrId), eq(role), any(InternalOrExternalUser.class));
    }

    @Test
    public void testShareEcmr_Success() throws Exception {
        // Arrange
        UUID ecmrId = UUID.randomUUID();
        String tan = "valid-tan";
        EcmrShareModel ecmrShareModel = new EcmrShareModel("recipient@example.com", EcmrRole.Consignee);

        ExternalUser externalUser = new ExternalUser(1L, "John", "Doe", "Example Company", "john.doe@example.com", "123456789", tan, Instant.now().plusSeconds(3600));

        Group group = new Group();
        EcmrShareResponse shareResponse = new EcmrShareResponse(ShareEcmrResult.SharedInternal, group); // Verwende Group

        when(authenticationService.getExternalUser(eq(ecmrId), eq(tan))).thenReturn(externalUser);
        when(ecmrShareService.shareEcmr(any(InternalOrExternalUser.class), eq(ecmrId), eq(ecmrShareModel.getEmail()), eq(ecmrShareModel.getRole()))).thenReturn(shareResponse);

        String jsonRequest = new ObjectMapper().writeValueAsString(ecmrShareModel);

        // Act
        mockMvc.perform(patch("/anonymous/ecmr/{ecmrId}/share", ecmrId).characterEncoding("UTF-8").param("tan", tan).contentType(MediaType.APPLICATION_JSON).content(jsonRequest)).andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getExternalUser(eq(ecmrId), eq(tan));
        verify(ecmrShareService, times(1)).shareEcmr(any(InternalOrExternalUser.class), eq(ecmrId), eq(ecmrShareModel.getEmail()), eq(ecmrShareModel.getRole()));
    }
}
