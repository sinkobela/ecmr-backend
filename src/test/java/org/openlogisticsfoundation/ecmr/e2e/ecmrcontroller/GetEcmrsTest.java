/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.e2e.ecmrcontroller;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.e2e.E2EBaseTest;
import org.openlogisticsfoundation.ecmr.e2e.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class GetEcmrsTest extends E2EBaseTest {

    static String validEcmrId = "";

    // Preparations
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
    @Order(1)
    void addSecondEcmr() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/ecmr/empty-ecmr2.json")
            )
            .queryParam("groupId", List.of(1))
            .port(randomServerPort)

            .when()
            .post("/api/ecmr")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    // Get my eCMRs
    @Test
    @Order(2)
    void getMyEcmrs_noParams() {
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
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("ecmrs", hasSize(2))
            .body("totalElements", is(2))
            .extract().response();

        List<EcmrModel> ecmrs = response.jsonPath().getList("ecmrs", EcmrModel.class);
        validEcmrId = ecmrs.getFirst().getEcmrId();
    }

    @Test
    @Order(2)
    void getMyEcmrs_specificFilterRequestModel() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/specific-filter-request-model.json")
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
    @Order(2)
    void getMyEcmrs_allParams() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("page", "0")
            .queryParam("size", "10")
            .queryParam("sortBy", "referenceId")
            .queryParam("sortingOrder", "ASC")
            .queryParam("type", "ECMR")
            .body(
                ResourceLoader.load("/json-objects/empty-filter-request-model.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/ecmr/my-ecmrs")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("ecmrs", hasSize(2))
            .body("totalElements", is(2));
    }

    @ParameterizedTest
    @CsvSource(
        {
            "page,0",
            "size,10",
            "sortBy,referenceId",
            "sortingOrder,ASC",
            "sortingOrder,DESC",
            "type,'ECMR'"
        }
    )
    @Order(2)
    void getMyEcmrs_oneParamProvided(String paramName, String paramValue) {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .queryParam(paramName, paramValue)
            .body(
                ResourceLoader.load("/json-objects/empty-filter-request-model.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/ecmr/my-ecmrs")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("ecmrs", hasSize(2))
            .body("totalElements", is(2));
    }

    @ParameterizedTest
    @CsvSource(
        {
            "type,'TEMPLATE'",
            "type,'ARCHIVED'"
        }
    )
    @Order(2)
    void getMyEcmrs_differentTypes(String paramName, String paramValue) {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .queryParam(paramName, paramValue)
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

    @Disabled("returns 500 but should return 400")
    @ParameterizedTest
    @CsvSource(
        {
            "page,-1",
            "size,0",
            "size,-1",
            "sortBy,xyz",
            "sortingOrder,asc",
            "sortingOrder,xyz",
            "type,'xyz'"
        }
    )
    @Order(2)
    void getMyEcmrs_invalidParams(String paramName, String paramValue) {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .queryParam(paramName, paramValue)
            .body(
                ResourceLoader.load("/json-objects/empty-filter-request-model.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/ecmr/my-ecmrs")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    // Get eCMRs
    @Test
    @Order(3)
    void getEcmr_valid() {
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

    @Test
    @Order(3)
    void getEcmr_invalid() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/ecmr/" + "xyz")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Order(3)
    void getEcmr_notFound() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/ecmr/" + "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
