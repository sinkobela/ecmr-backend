/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.e2e.ecmrcontroller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.e2e.E2EBaseTest;
import org.openlogisticsfoundation.ecmr.e2e.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateEcmrTest extends E2EBaseTest {

    static String ecmrId;

    // Preparation
    @Test
    @Order(1)
    void addEcmr() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/ecmr/empty-ecmr.json")
            )
            .queryParam("groupId", List.of(1))
            .port(randomServerPort)

            .when()
            .post("/api/ecmr")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @Order(2)
    void getEcmrs() {
        Response response = given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/empty-filter-request-model.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/ecmr/my-ecmrs")

            .then()
            .extract().response();

        List<EcmrModel> ecmrs = response.jsonPath().getList("ecmrs", EcmrModel.class);
        ecmrId = ecmrs.getFirst().getEcmrId();
    }

    // Update eCMR
    @Test
    void updateEcmr_valid() throws JsonProcessingException {
        EcmrModel ecmrModel = new ObjectMapper()
            .readValue(ResourceLoader.load("/json-objects/ecmr/updated-ecmr.json"), EcmrModel.class);
        ecmrModel.setEcmrId(ecmrId);
        String requestBody = new ObjectMapper().writeValueAsString(ecmrModel);

        Response response = given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(requestBody)
            .port(randomServerPort)

            .when()
            .put("/api/ecmr")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract().response();

        EcmrModel result = response.getBody().as(EcmrModel.class);

        assertEquals(result.getEcmrConsignment().getSenderInformation().getSenderNamePerson(), "sender");
    }

    @Test
    @Disabled("returns 500 but should return 400")
    void updateEcmr_invalidId() throws JsonProcessingException {
        EcmrModel ecmrModel = new ObjectMapper()
            .readValue(ResourceLoader.load("/json-objects/ecmr/updated-ecmr.json"), EcmrModel.class);
        ecmrModel.setEcmrId("xyz");
        String requestBody = new ObjectMapper().writeValueAsString(ecmrModel);

        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(requestBody)
            .port(randomServerPort)

            .when()
            .put("/api/ecmr")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Disabled("returns 500 but should return 404")
    void updateEcmr_idNotFound() throws JsonProcessingException {
        EcmrModel ecmrModel = new ObjectMapper()
            .readValue(ResourceLoader.load("/json-objects/ecmr/updated-ecmr.json"), EcmrModel.class);
        ecmrModel.setEcmrId("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx");
        String requestBody = new ObjectMapper().writeValueAsString(ecmrModel);

        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(requestBody)
            .port(randomServerPort)

            .when()
            .put("/api/ecmr")

            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void updateEcmr_invalidUpdateValue() throws JsonProcessingException {
        EcmrModel ecmrModel = new ObjectMapper()
            .readValue(ResourceLoader.load("/json-objects/ecmr/invalid-ecmr.json"), EcmrModel.class);
        ecmrModel.setEcmrId(ecmrId);
        String requestBody = new ObjectMapper().writeValueAsString(ecmrModel);

        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(requestBody)
            .port(randomServerPort)

            .when()
            .put("/api/ecmr")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    // Archive ecmr (not finished)
    @Test
    @Order(3)
    void archiveEcmr_valid() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .patch("/api/ecmr/" + ecmrId + "/archive")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);

        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("type", "ARCHIVED")
            .body(
                ResourceLoader.load("/json-objects/empty-filter-request-model.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/ecmr/my-ecmrs")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("ecmrs", hasSize(1))
            .body("totalElements", is(1));
    }

    @Test
    void archiveEcmr_invalidId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .patch("/api/ecmr/" + "xyz" + "/archive")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void archiveEcmr_idNotFound() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .patch("/api/ecmr/" + "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" + "/archive")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    // Reactivate eCMR
    @Test
    @Order(4)
    void reactivateEcmr_valid() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .patch("/api/ecmr/" + ecmrId + "/reactivate")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);

        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("type", "ARCHIVED")
            .body(
                ResourceLoader.load("/json-objects/empty-filter-request-model.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/ecmr/my-ecmrs")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("ecmrs", hasSize(0))
            .body("totalElements", is(0));
    }

    @Test
    void reactivateEcmr_invalidId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .patch("/api/ecmr/" + "xyz" + "/reactivate")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void reactivateEcmr_idNotFound() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .patch("/api/ecmr/" + "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" + "/reactivate")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }


}
