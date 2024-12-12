/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.e2e.externalcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openlogisticsfoundation.ecmr.api.model.SealedDocument;
import org.openlogisticsfoundation.ecmr.domain.services.ExternalEcmrInstanceService;
import org.openlogisticsfoundation.ecmr.e2e.E2EBaseTest;
import org.openlogisticsfoundation.ecmr.e2e.ResourceLoader;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ImportEcmrTest extends E2EBaseTest {

    @MockBean
    ExternalEcmrInstanceService externalEcmrInstanceService;

    private static String ecmrId;
    private static final String ecmrIdWithInvalidSeal = "12345678-1111-2222-3333-098765432109";

    @BeforeEach
    public void mockExternalInstance() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        SealedDocument sealedDocument = objectMapper.readValue(ResourceLoader.load("/json-objects/sealed-document.json"), SealedDocument.class);
        when(externalEcmrInstanceService.importEcmr(any(String.class), eq(UUID.fromString(sealedDocument.getSealedEcmr().getEcmr().getEcmrId())), any(String.class))).thenReturn(sealedDocument);
        ecmrId = sealedDocument.getSealedEcmr().getEcmr().getEcmrId();

        SealedDocument invalidSealedDocument = objectMapper.readValue(ResourceLoader.load("/json-objects/sealed-document.json"), SealedDocument.class);
        invalidSealedDocument.getSealedEcmr().getEcmr().setEcmrId(ecmrIdWithInvalidSeal);
        invalidSealedDocument.setSeal("invalidSeal");
        when(externalEcmrInstanceService.importEcmr(any(String.class), eq(UUID.fromString(ecmrIdWithInvalidSeal)), any(String.class))).thenReturn(invalidSealedDocument);
    }

    @Test
    public void importEcmr_valid(){
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("shareToken", "token")
            .queryParam("ecmrId", ecmrId)
            .queryParam("url", "url")
            .queryParam("groupId", List.of(1))
            .port(randomServerPort)

            .when()
            .post("/api/external/ecmr/import")

            .then()
            .statusCode(200);

        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/ecmr/" + ecmrId)

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("ecmrId", is(ecmrId));
    }

    @Test
    public void importEcmr_invalidSeal(){
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("shareToken", "token")
            .queryParam("ecmrId", ecmrIdWithInvalidSeal)
            .queryParam("url", "url")
            .queryParam("groupId", List.of(1))
            .port(randomServerPort)

            .when()
            .post("/api/external/ecmr/import")

            .then()
            .statusCode(400);
    }

    @Test
    public void importEcmr_invalidGroupIds(){
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("shareToken", "token")
            .queryParam("ecmrId", ecmrId)
            .queryParam("url", "url")
            .queryParam("groupId", List.of(100))
            .port(randomServerPort)

            .when()
            .post("/api/external/ecmr/import")

            .then()
            .statusCode(403);
    }


}
