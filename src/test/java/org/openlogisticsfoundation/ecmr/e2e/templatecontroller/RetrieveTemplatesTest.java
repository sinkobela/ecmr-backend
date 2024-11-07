/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.e2e.templatecontroller;

import io.restassured.response.Response;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openlogisticsfoundation.ecmr.domain.models.TemplateUser;
import org.openlogisticsfoundation.ecmr.e2e.E2EBaseTest;
import org.openlogisticsfoundation.ecmr.e2e.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.given;

public class RetrieveTemplatesTest extends E2EBaseTest {

    static Long validTemplateId;

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

        validTemplateId = response.as(TemplateUser.class).getId();
    }

    // Get all templates
    @Test
    void getAllTemplates_valid() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/template")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    // Get template
    @Test
    void getTemplate_valid() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/template/" + validTemplateId)

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void getTemplate_invalidId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/template/" + "xyz")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void getTemplate_wrongId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/template/" + "1234")

            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

}
