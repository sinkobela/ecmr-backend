/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.e2e.externalcontroller;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrExportResult;
import org.openlogisticsfoundation.ecmr.e2e.E2EBaseTest;
import org.openlogisticsfoundation.ecmr.e2e.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.response.Response;

class ExportEcmrTest extends E2EBaseTest {

    static String validEcmrId;
    static String validShareToken;

    // Preparations
    // create ecmr
    @Test
    @Order(1)
    void addEcmr() {
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
        validEcmrId = ecmr.getEcmrId();
    }

    // seal ecmr
    @Test
    @Order(2)
    void sealEcmr_valid() {
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
            .post("/api/ecmr/"+validEcmrId+"/seal")

            .then()
            .statusCode(HttpStatus.OK.value());
    }

    // get share token
    @Test
    @Order(3)
    void getShareToken_valid() {
        Response response = given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("ecmrRole", "Reader")
            .port(randomServerPort)

            .when()
            .get("/api/ecmr/"+validEcmrId+"/share-token")

            .then()
            .statusCode(200)
            .extract().response();

        validShareToken = response.asString();
    }

    // EXPORT
    @Test
    @Order(4)
    void exportEcmr_valid(){
        Response response = given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .queryParam("shareToken", validShareToken)
            .port(randomServerPort)

            .when()
            .get("/api/external/ecmr/"+validEcmrId+"/export")

            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract().response();

        EcmrExportResult result = response.as(EcmrExportResult.class);
        assertEquals(validEcmrId, result.getSealedDocument().getEcmr().getEcmrId());
    }

    @Test
    @Order(4)
    void exportEcmr_ecmrIdInvalid(){
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .queryParam("shareToken", validShareToken)
            .port(randomServerPort)

            .when()
            .get("/api/external/ecmr/"+"00000000-0000-0000-0000-000000000000"+"/export")

            .then()
            .statusCode(404);
    }

    @Test
    @Order(4)
    void exportEcmr_shareTokenInvalid(){
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .queryParam("shareToken", "invalidShareToken")
            .port(randomServerPort)

            .when()
            .get("/api/external/ecmr/"+validEcmrId+"/export")

            .then()
            .statusCode(403);
    }

}
