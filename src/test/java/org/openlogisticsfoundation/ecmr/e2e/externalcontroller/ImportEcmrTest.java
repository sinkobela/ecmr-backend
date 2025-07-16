/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.e2e.externalcontroller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.EcmrSeal;
import org.openlogisticsfoundation.ecmr.api.model.SealedDocument;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrExportResult;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.domain.services.ExternalEcmrInstanceService;
import org.openlogisticsfoundation.ecmr.e2e.E2EBaseTest;
import org.openlogisticsfoundation.ecmr.e2e.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.restassured.response.Response;
//TODO activate and fix, when new sharing process is implemented
@Disabled()
class ImportEcmrTest extends E2EBaseTest {

    @MockitoBean
    ExternalEcmrInstanceService externalEcmrInstanceService;

    private static final String ecmrIdWithInvalidSeal = "12345678-1111-2222-3333-098765432109";
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static String validEcmrId;
    private static String validSeal;

    @BeforeEach
    void mockExternalInstance() throws Exception {
        SealedDocument invalidSealedDocument = objectMapper.readValue(ResourceLoader.load("/json-objects/sealed-document.json"),
                SealedDocument.class);
        invalidSealedDocument.getEcmr().setEcmrId(ecmrIdWithInvalidSeal);
        EcmrSeal invalidSeal = new EcmrSeal();
        invalidSeal.setSeal("invalidSeal");
        invalidSealedDocument.setSenderSeal(invalidSeal);
        EcmrExportResult invalidResult = new EcmrExportResult(invalidSealedDocument, EcmrRole.Carrier);
        when(externalEcmrInstanceService.importEcmr(any(String.class), eq(UUID.fromString(ecmrIdWithInvalidSeal)), any(String.class))).thenReturn(
                invalidResult);
    }

    // Preparations: create a valid seal to use it for the test ecmr later
    @Test
    @Order(0)
    void getValidSeal() {
        Response response = given()
                .accept(String.valueOf(MediaType.APPLICATION_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + adminToken)
                .body(
                        ResourceLoader.load("/json-objects/ecmr/full-ecmr.json")
                )
                .queryParam("groupId", List.of(1))
                .port(randomServerPort)

                .when()
                .post("/api/ecmr")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract().response();

        EcmrModel ecmr = response.as(EcmrModel.class);
        String ecmrId = ecmr.getEcmrId();


        Response shareTokenResponse = given()
                .accept(String.valueOf(MediaType.APPLICATION_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("ecmrRole", "Sender")
                .port(randomServerPort)

                .when()
                .get("/api/ecmr/" + ecmrId + "/share-token")

                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract().response();

        String shareToken = shareTokenResponse.asString();

        given()
                .accept(String.valueOf(MediaType.APPLICATION_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + adminToken)
                .body(
                        """
                                {
                                    "transportRole":"SENDER",
                                    "city":"dortmund"
                                }
                                """
                )
                .port(randomServerPort)

                .when()
                .log().all()
                .post("/api/ecmr/" + ecmrId + "/seal")

                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract().response();

        Response sealResponse = given()
                .accept(String.valueOf(MediaType.APPLICATION_JSON))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("shareToken", shareToken)
                .port(randomServerPort)

                .when()
                .get("/api/external/ecmr/" + ecmrId + "/export")

                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract().response();

        EcmrExportResult exportResult = sealResponse.as(EcmrExportResult.class);
        validSeal = exportResult.getSealedDocument().getSenderSeal().getSeal();
    }

    @Test
    @Order(1)
    void importEcmr_invalidSeal() {
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
    @Order(1)
    void importEcmr_invalidGroupIds() throws JsonProcessingException {
        SealedDocument sealedDocument = objectMapper.readValue(ResourceLoader.load("/json-objects/sealed-document.json"), SealedDocument.class);
        validEcmrId = sealedDocument.getEcmr().getEcmrId();
        sealedDocument.getSenderSeal().setSeal(validSeal);
        EcmrExportResult result = new EcmrExportResult(sealedDocument, EcmrRole.Carrier);
        when(externalEcmrInstanceService.importEcmr(any(String.class), eq(UUID.fromString(validEcmrId)), any(String.class))).thenReturn(result);

        given()
                .accept(String.valueOf(MediaType.APPLICATION_JSON))
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("shareToken", "token")
                .queryParam("ecmrId", validEcmrId)
                .queryParam("url", "url")
                .queryParam("groupId", List.of(100))
                .port(randomServerPort)

                .when()
                .post("/api/external/ecmr/import")

                .then()
                .statusCode(403);
    }

    @Test
    @Order(2)
    void importEcmr_valid() throws JsonProcessingException {
        SealedDocument sealedDocument = objectMapper.readValue(ResourceLoader.load("/json-objects/sealed-document.json"), SealedDocument.class);
        validEcmrId = sealedDocument.getEcmr().getEcmrId();
        sealedDocument.getSenderSeal().setSeal(validSeal);
        EcmrExportResult result = new EcmrExportResult(sealedDocument, EcmrRole.Carrier);
        when(externalEcmrInstanceService.importEcmr(any(String.class), eq(UUID.fromString(validEcmrId)), any(String.class))).thenReturn(result);

        given()
                .accept(String.valueOf(MediaType.APPLICATION_JSON))
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("shareToken", "token")
                .queryParam("ecmrId", validEcmrId)
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
                .get("/api/ecmr/" + validEcmrId)

                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("ecmrId", is(validEcmrId));
    }

}
