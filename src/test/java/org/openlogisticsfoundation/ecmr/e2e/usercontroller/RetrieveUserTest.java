/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.e2e.usercontroller;

import io.restassured.response.Response;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openlogisticsfoundation.ecmr.e2e.E2EBaseTest;
import org.openlogisticsfoundation.ecmr.e2e.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class RetrieveUserTest extends E2EBaseTest {

    static String validUserId;

    // Preparation
    @Test
    @Order(1)
    void createUser_valid() {
        Response response = given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/user/valid-user.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/user")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract().response();

        validUserId = response.getBody().jsonPath().getString("id");
    }

    // Get current user
    @Test
    void getCurrent() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/user/current")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    // Get all mails
    @Test
    void getAllMails() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/user/mail")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    // Get all users
    @Test
    void getAllUsers() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/user")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("size()", is(3));
    }

    // Get groups
    @Test
    void getGroups_valid() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/user/" + validUserId + "/groups")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("size()", is(1));
    }

    @Test
    void getGroups_wrongRole() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + userToken)
            .port(randomServerPort)

            .when()
            .get("/api/user/" + validUserId + "/groups")

            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void getGroups_invalidId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/user/" + "xyz" + "/groups")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void getGroups_wrongId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/user/" + "12438905802" + "/groups")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("size()", is(0));
    }

}
