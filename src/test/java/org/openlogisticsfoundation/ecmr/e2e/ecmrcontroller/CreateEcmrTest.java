/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.e2e.ecmrcontroller;

import org.junit.jupiter.api.*;
import org.openlogisticsfoundation.ecmr.e2e.E2EBaseTest;
import org.openlogisticsfoundation.ecmr.e2e.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static io.restassured.RestAssured.given;

public class CreateEcmrTest extends E2EBaseTest {

    @Test
    @Order(1)
    void createEcmr_valid() {
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
    void createEcmr_noGroup() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/ecmr/empty-ecmr.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/ecmr")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createEcmr_wrongGroup() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/ecmr/empty-ecmr.json")
            )
            .queryParam("groupId", List.of(100))
            .port(randomServerPort)

            .when()
            .post("/api/ecmr")

            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void createEcmr_invalidBody() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/ecmr/invalid-ecmr.json")
            )
            .queryParam("groupId", List.of(1))
            .port(randomServerPort)

            .when()
            .post("/api/ecmr")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createEcmr_noBody() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("groupId", List.of(1))
            .port(randomServerPort)

            .when()
            .post("/api/ecmr")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
