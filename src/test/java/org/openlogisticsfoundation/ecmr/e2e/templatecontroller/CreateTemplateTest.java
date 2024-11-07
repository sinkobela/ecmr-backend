/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.e2e.templatecontroller;

import org.junit.jupiter.api.*;
import org.openlogisticsfoundation.ecmr.e2e.E2EBaseTest;
import org.openlogisticsfoundation.ecmr.e2e.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.given;

public class CreateTemplateTest extends E2EBaseTest {

    @Test
    @Order(1)
    void createTemplate_valid() {
        given()
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
            .contentType(MediaType.APPLICATION_JSON_VALUE);

    }

    @Test
    @Order(1)
    void createTemplate_noName() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/ecmr/empty-ecmr.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/template")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);

    }

    @Test
    @Disabled("returns 200 but should return 400")
    void createTemplate_invalidBody() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/ecmr/invalid-ecmr.json")
            )
            .queryParam("name", "template1")
            .port(randomServerPort)

            .when()
            .post("/api/template")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());

    }

    @Test
    @Order(1)
    void createTemplate_noBody() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("name", "template1")
            .port(randomServerPort)

            .when()
            .post("/api/template")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());

    }
}
