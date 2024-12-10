/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.controllers;

import org.junit.jupiter.api.Test;
import org.openlogisticsfoundation.ecmr.api.model.EcmrConsignment;
import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.api.model.signature.Signature;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.SealCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.SignCommand;
import org.openlogisticsfoundation.ecmr.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.domain.models.*;
import org.openlogisticsfoundation.ecmr.domain.services.*;
import org.openlogisticsfoundation.ecmr.web.mappers.EcmrWebMapper;
import org.openlogisticsfoundation.ecmr.web.services.AuthenticationService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext
public class EcmrControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EcmrService ecmrService;

    @MockBean
    private EcmrUpdateService ecmrUpdateService;

    @MockBean
    private EcmrCreationService ecmrCreationService;

    @MockBean
    private EcmrPdfService ecmrPdfService;

    @MockBean
    private EcmrWebMapper ecmrWebMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private EcmrShareService ecmrShareService;

    @MockBean
    private EcmrSignService ecmrSignService;

    private AuthenticatedUser authenticatedUser;
    private UUID ecmrId;
    private EcmrModel ecmrModel;
    private SignModel signModel;
    private SealModel sealModel;
    private EcmrCommand ecmrCommand;
    private List<Long> groupIds;
    private String jsonRequest;

    @BeforeEach
    public void setup() throws Exception {
             User user = new User(
            1L,
            "John",
            "Doe",
            CountryCode.DE,
            "john.doe@example.com",
            "123456789",
            UserRole.User,
            123L,
            false,
            false
        );
        ecmrId = UUID.randomUUID();
        ecmrModel = new EcmrModel();
        ecmrModel.setEcmrId(ecmrId.toString());
        ecmrModel.setEcmrConsignment(new EcmrConsignment());
        signModel = new SignModel(Signer.Consignee, "signatureData", "Sample City");
        sealModel = new SealModel(Signer.Consignee, null, "Sample City");
        ecmrCommand = mock(EcmrCommand.class);
        groupIds = List.of(1L, 2L);
        jsonRequest = new ObjectMapper().writeValueAsString(ecmrModel);
        authenticatedUser = new AuthenticatedUser(user);

        when(authenticationService.getAuthenticatedUser()).thenReturn(authenticatedUser);
    }

    @Test
    @WithMockUser
    public void testGetMyEcmrs_Success() throws Exception {
        // Arrange
        FilterRequestModel filterRequestModel = new FilterRequestModel(
            "referenceId",
            "from",
            "to",
            EcmrTransportType.National,
            EcmrStatus.NEW,
            "licensePlate",
            "carrierName",
            "carrierPostCode",
            "consigneePostCode",
            "lastEditor"
        );
        EcmrPageModel pageModel = new EcmrPageModel(0,1,List.of());

        when(ecmrService.getEcmrsForUser(any(), any(), anyInt(), anyInt(), any(), any(), any())).thenReturn(pageModel);
        String filterJsonRequest = new ObjectMapper().writeValueAsString(filterRequestModel);

        // Act
        mockMvc.perform(post("/ecmr/my-ecmrs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(filterJsonRequest))
            .andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getAuthenticatedUser();
        verify(ecmrService, times(1)).getEcmrsForUser(any(), any(), anyInt(), anyInt(), any(), any(), any());
    }

    @Test
    @WithMockUser
    public void testGetEcmr_Success() throws Exception {
        // Arrange
        when(authenticationService.getAuthenticatedUser(true)).thenReturn(authenticatedUser);
        when(ecmrService.getEcmr(eq(ecmrId), any())).thenReturn(ecmrModel);

        // Act
        mockMvc.perform(get("/ecmr/{ecmrId}", ecmrId)).andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getAuthenticatedUser();
        verify(ecmrService, times(1)).getEcmr(eq(ecmrId), any());
    }

    @Test
    @WithMockUser
    public void testCreateEcmr_Success() throws Exception {
        // Arrange
        when(authenticationService.getAuthenticatedUser(true)).thenReturn(authenticatedUser);
        when(ecmrWebMapper.toCommand(ecmrModel)).thenReturn(ecmrCommand);

        // Act
        mockMvc.perform(post("/ecmr").param("groupId", "1,2").contentType(MediaType.APPLICATION_JSON).content(jsonRequest)).andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getAuthenticatedUser(true);
        verify(ecmrWebMapper, times(1)).toCommand(ecmrModel);
        verify(ecmrCreationService, times(1)).createEcmr(eq(ecmrCommand), eq(authenticatedUser), eq(groupIds));
    }

    @Test
    @WithMockUser
    public void testDeleteEcmr_Success() throws Exception {
        // Act
        mockMvc.perform(delete("/ecmr/{ecmrId}", ecmrId)).andExpect(status().isNoContent());

        // Assert
        verify(authenticationService, times(1)).getAuthenticatedUser();
        verify(ecmrService, times(1)).deleteEcmr(eq(ecmrId), any());
    }

    @Test
    @WithMockUser
    public void testArchiveEcmr_Success() throws Exception {
        // Arrange
        when(ecmrUpdateService.archiveEcmr(eq(ecmrId), eq(authenticatedUser))).thenReturn(ecmrModel);

        // Act
        mockMvc.perform(patch("/ecmr/{ecmrId}/archive", ecmrId)).andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getAuthenticatedUser();
        verify(ecmrUpdateService, times(1)).archiveEcmr(eq(ecmrId), eq(authenticatedUser));
    }

    @Test
    @WithMockUser
    public void testReactivateEcmr_Success() throws Exception {
        // Arrange
        when(ecmrUpdateService.reactivateEcmr(eq(ecmrId), eq(authenticatedUser))).thenReturn(ecmrModel);

        // Act
        mockMvc.perform(patch("/ecmr/{ecmrId}/reactivate", ecmrId)).andExpect(status().isOk());


        // Assert
        verify(authenticationService, times(1)).getAuthenticatedUser();
        verify(ecmrUpdateService, times(1)).reactivateEcmr(eq(ecmrId), eq(authenticatedUser));
    }

    @Test
    @WithMockUser
    public void testShareEcmr_Success() throws Exception {
        //Arrange
        EcmrShareModel ecmrShareModel = new EcmrShareModel("test@example.com",EcmrRole.Carrier);
        EcmrShareResponse ecmrShareResponse = new EcmrShareResponse(ShareEcmrResult.SharedExternal,new Group());

        when(authenticationService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(ecmrShareService.shareEcmr(any(InternalOrExternalUser.class), any(UUID.class), any(String.class), any(EcmrRole.class)))
            .thenReturn(ecmrShareResponse);

        //Act
        mockMvc.perform(patch("/ecmr/{ecmrId}/share", ecmrId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(ecmrShareModel)))
                .andExpect(status().isOk());

        //Assert
        verify(authenticationService, times(1)).getAuthenticatedUser();
        verify((ecmrShareService), times(1)).shareEcmr(any(InternalOrExternalUser.class), any(UUID.class), any(String.class), any(EcmrRole.class));
    }

    @Test
    @WithMockUser
    public void testImportEcmr_Success() throws Exception {
        // Arrange
        String shareToken = "valid-share-token";

        when(ecmrShareService.importEcmr(eq(authenticatedUser), eq(ecmrId), eq(shareToken))).thenReturn(ecmrModel);

        // Act
        mockMvc.perform(get("/ecmr/{ecmrId}/import", ecmrId).param("shareToken", shareToken)).andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getAuthenticatedUser();
        verify(ecmrShareService, times(1)).importEcmr(eq(authenticatedUser), eq(ecmrId), eq(shareToken));
    }

    @Test
    @WithMockUser
    public void testDownloadEcmrPdfFile_Success() throws Exception {
        // Arrange
        String filename = "test.pdf";
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        PdfFile pdfFile = new PdfFile(filename, data);

        when(authenticationService.getAuthenticatedUser(true)).thenReturn(authenticatedUser);
        when(ecmrService.createJasperReportForEcmr(eq(ecmrId), any(InternalOrExternalUser.class))).thenReturn(pdfFile);

        // Act & Assert
        mockMvc.perform(get("/ecmr/{ecmrId}/pdf", ecmrId))
            .andExpect(status().isOk());

        verify(authenticationService, times(1)).getAuthenticatedUser(true);
        verify(ecmrService, times(1)).createJasperReportForEcmr(eq(ecmrId), any(InternalOrExternalUser.class));
    }

    @Test
    @WithMockUser
    public void testUpdateEcmr_Success() throws Exception {
        // Arrange
        when(ecmrWebMapper.toCommand(ecmrModel)).thenReturn(ecmrCommand);
        when(ecmrUpdateService.updateEcmr(eq(ecmrCommand), any(), any())).thenReturn(ecmrModel);

        // Act
        mockMvc.perform(put("/ecmr").
                contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getAuthenticatedUser();
        verify(ecmrWebMapper, times(1)).toCommand(ecmrModel);
        verify(ecmrUpdateService, times(1)).updateEcmr(eq(ecmrCommand), any(), any());
    }

    @Test
    @WithMockUser
    public void testSignOnGlass_Success() throws Exception {
        // Arrange
        Signature signature = new Signature();
        SignCommand signCommand = new SignCommand(Signer.Sender, "signatureData", "Sample City");

        when(ecmrWebMapper.map(any(SignModel.class))).thenReturn(signCommand);
        when(ecmrSignService.signEcmr(any(), eq(ecmrId), eq(signCommand), eq(SignatureType.SignOnGlass))).thenReturn(signature);

        String signJsonRequest = new ObjectMapper().writeValueAsString(signModel);

        // Act
        mockMvc.perform(post("/ecmr/{ecmrId}/sign-on-glass", ecmrId).contentType(MediaType.APPLICATION_JSON).content(signJsonRequest)).andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getAuthenticatedUser();
        verify(ecmrWebMapper, times(1)).map(any(SignModel.class));
        verify(ecmrSignService, times(1)).signEcmr(any(), eq(ecmrId), eq(signCommand), eq(SignatureType.SignOnGlass));
    }

    @Test
    @WithMockUser
    public void testESeal_Success() throws Exception {
        // Arrange
        Signature signature = new Signature();
        SealCommand sealCommand = new SealCommand(Signer.Sender, null, "Sample City");

        when(ecmrWebMapper.map(any(SealModel.class))).thenReturn(sealCommand);
        when(ecmrSignService.sealEcmr(any(), eq(ecmrId), eq(sealCommand), eq(SignatureType.ESeal))).thenReturn(signature);

        String signJsonRequest = new ObjectMapper().writeValueAsString(sealModel);

        // Act
        mockMvc.perform(post("/ecmr/{ecmrId}/seal", ecmrId).contentType(MediaType.APPLICATION_JSON).content(signJsonRequest)).andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getAuthenticatedUser();
        verify(ecmrWebMapper, times(1)).map(any(SealModel.class));
        verify(ecmrSignService, times(1)).sealEcmr(any(), eq(ecmrId), eq(sealCommand), eq(SignatureType.ESeal));
    }

    @Test
    @WithMockUser
    public void testGetShareToken_Success() throws Exception {
        // Arrange
        EcmrRole ecmrRole = EcmrRole.Sender;
        String shareToken = "valid-share-token";

        when(ecmrShareService.getShareToken(eq(ecmrId), eq(ecmrRole), any())).thenReturn(shareToken);

        // Act
        mockMvc.perform(get("/ecmr/{ecmrId}/share-token", ecmrId).param("ecmrRole", ecmrRole.name())).andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getAuthenticatedUser();
        verify(ecmrShareService, times(1)).getShareToken(eq(ecmrId), eq(ecmrRole), any());
    }

    @Test
    @WithMockUser
    public void testGetCurrentEcmrRoles_Success() throws Exception {
        // Arrange
        List<EcmrRole> roles = List.of(EcmrRole.Sender, EcmrRole.Consignee);

        when(ecmrService.getCurrentEcmrRoles(eq(ecmrId), any())).thenReturn(roles);

        // Act
        mockMvc.perform(get("/ecmr/{ecmrId}/role", ecmrId)).andExpect(status().isOk());

        // Assert
        verify(authenticationService, times(1)).getAuthenticatedUser();
        verify(ecmrService, times(1)).getCurrentEcmrRoles(eq(ecmrId), any());
    }
}
