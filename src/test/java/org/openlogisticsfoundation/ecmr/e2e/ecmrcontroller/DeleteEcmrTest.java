/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.e2e.ecmrcontroller;

import io.restassured.response.Response;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.e2e.E2EBaseTest;
import org.openlogisticsfoundation.ecmr.e2e.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class DeleteEcmrTest extends E2EBaseTest {

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

    // Delete eCMR
    @Test
    @Order(2)
    void deleteEcmr_wrongId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .delete("/api/ecmr/" + "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Order(2)
    void deleteEcmr_invalidId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .delete("/api/ecmr/" + "xyz")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Order(3)
    void deleteEcmr_valid() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .delete("/api/ecmr/" + ecmrId)

            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        given()
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
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("ecmrs", hasSize(0))
            .body("totalElements", is(0));
    }
}
