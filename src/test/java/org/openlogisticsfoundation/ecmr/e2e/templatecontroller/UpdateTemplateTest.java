/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.e2e.templatecontroller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.domain.models.TemplateUser;
import org.openlogisticsfoundation.ecmr.e2e.E2EBaseTest;
import org.openlogisticsfoundation.ecmr.e2e.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.given;

public class UpdateTemplateTest extends E2EBaseTest {

    static TemplateUser validTemplateUser;

    // Preparation
    @Test
    @Order(1)
    void createTemplate_valid() {
        Response response = given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/ecmr/empty-ecmr.json")
            )
            .queryParam("name", "template1")
            .port(randomServerPort)

            .when()
            .post("/api/template")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract().response();

        validTemplateUser = response.as(TemplateUser.class);
    }

    // Update template
    @Test
    @Order(2)
    void updateTemplate_valid() throws JsonProcessingException {
        EcmrModel ecmrModel = new ObjectMapper()
            .readValue(ResourceLoader.load("/json-objects/ecmr/updated-ecmr.json"), EcmrModel.class);
        validTemplateUser.setEcmr(ecmrModel);
        String requestBody = new ObjectMapper().writeValueAsString(validTemplateUser);

        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(requestBody)
            .port(randomServerPort)

            .when()
            .patch("/api/template")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);

    }

    @Test
    @Disabled("returns 200 but should return 400")
    void updateTemplate_invalidBody() throws JsonProcessingException {
        EcmrModel ecmrModel = new ObjectMapper()
            .readValue(ResourceLoader.load("/json-objects/ecmr/invalid-ecmr.json"), EcmrModel.class);
        validTemplateUser.setEcmr(ecmrModel);
        String requestBody = new ObjectMapper().writeValueAsString(validTemplateUser);

        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(requestBody)
            .port(randomServerPort)

            .when()
            .patch("/api/template")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void updateTemplate_noBody() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .patch("/api/template")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void updateTemplate_wrongId() throws JsonProcessingException {
        EcmrModel ecmrModel = new ObjectMapper()
            .readValue(ResourceLoader.load("/json-objects/ecmr/updated-ecmr.json"), EcmrModel.class);
        validTemplateUser.setEcmr(ecmrModel);
        validTemplateUser.setId(123532L);
        String requestBody = new ObjectMapper().writeValueAsString(validTemplateUser);

        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(requestBody)
            .port(randomServerPort)

            .when()
            .patch("/api/template")

            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());

    }

}
