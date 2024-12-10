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
import org.openlogisticsfoundation.ecmr.api.model.signature.Signature;
import org.openlogisticsfoundation.ecmr.e2e.E2EBaseTest;
import org.openlogisticsfoundation.ecmr.e2e.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static io.restassured.RestAssured.given;

public class SealEcmrTest extends E2EBaseTest {
    static String validEcmrId;
    static String secondValidEcmrId;
    static String validEcmrIdEmpty;
    static String seal;

    // Preparations
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

    @Test
    @Order(1)
    void addSecondEcmr() {
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
        secondValidEcmrId = ecmr.getEcmrId();
    }

    @Test
    @Order(1)
    void addEmptyEcmr() {
        Response response = given()
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
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract().response();

        EcmrModel ecmr = response.as(EcmrModel.class);
        validEcmrIdEmpty = ecmr.getEcmrId();
    }

    @Test
    @Order(2)
    void sealEcmr_wrongSigner() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                """
                    {
                        "signer":"WrongSender",
                        "precedingSeal":null,
                        "city":"dortmund"
                    }
                    """
            )
            .port(randomServerPort)

            .when()
            .log().all()
            .post("/api/ecmr/"+validEcmrId+"/seal")

            .then()
            .log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @Order(2)
    void sealEcmr_emptyEcmr() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                """
                    {
                        "signer":"Sender",
                        "precedingSeal":null,
                        "city":"dortmund"
                    }
                    """
            )
            .port(randomServerPort)

            .when()
            .log().all()
            .post("/api/ecmr/"+validEcmrIdEmpty+"/seal")

            .then()
            .log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @Order(2)
    void sealEcmr_valid() {
        Response response = given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                """
                    {
                        "signer":"Sender",
                        "precedingSeal":null,
                        "city":"dortmund"
                    }
                    """
            )
            .port(randomServerPort)

            .when()
            .log().all()
            .post("/api/ecmr/"+validEcmrId+"/seal")

            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract().response();

        Signature signature = response.as(Signature.class);
        seal = signature.getData();
    }

    @Test
    @Order(3)
    void sealEcmr_withPrecedingSeal() {
        Response response = given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                """
                    {
                        "signer":"Sender",
                        "precedingSeal":"%s",
                        "city":"dortmund"
                    }
                    """.formatted(seal)
            )
            .port(randomServerPort)

            .when()
            .log().all()
            .post("/api/ecmr/"+secondValidEcmrId+"/seal")

            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract().response();

        Signature signature = response.as(Signature.class);
        seal = signature.getData();
    }
}
