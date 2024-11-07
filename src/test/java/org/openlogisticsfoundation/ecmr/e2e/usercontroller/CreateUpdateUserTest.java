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

public class CreateUpdateUserTest extends E2EBaseTest {

    static String validUserId;

    // Create user
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

    @Test
    void createUser_wrongRole() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + userToken)
            .body(
                ResourceLoader.load("/json-objects/user/valid-user.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/user")

            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void createUser_invalid() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/user/invalid-user.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/user")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    // Update user
    @Test
    @Order(2)
    void updateUser_valid() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/user/updated-valid-user.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/user/" + validUserId)

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("firstName", is("Manfred"));
    }

    @Test
    @Order(2)
    void updateUser_wrongRole() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + userToken)
            .body(
                ResourceLoader.load("/json-objects/user/updated-valid-user.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/user/" + validUserId)

            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @Order(2)
    void updateUser_wrongId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/user/updated-valid-user.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/user/" + "23950215")

            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @Order(2)
    void updateUser_invalidId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/user/updated-valid-user.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/user/" + "xyz")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @Order(2)
    void updateUser_invalidBody() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body(
                ResourceLoader.load("/json-objects/user/invalid-user.json")
            )
            .port(randomServerPort)

            .when()
            .post("/api/user/" + validUserId)

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    // Deactivate user
    @Test
    @Order(2)
    void deactivateUser_valid() {
        given()
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .post("/api/user/" + validUserId + "/deactivate")

            .then()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    @Order(2)
    void deactivateUser_wrongRole() {
        given()
            .header("Authorization", "Bearer " + userToken)
            .port(randomServerPort)

            .when()
            .post("/api/user/" + validUserId + "/deactivate")

            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @Order(2)
    void deactivateUser_invalidId() {
        given()
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .post("/api/user/" + "XY" + "/deactivate")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Order(2)
    void deactivateUser_wrongId() {
        given()
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .post("/api/user/" + "20498590438" + "/deactivate")

            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    // Activate user
    @Test
    @Order(3)
    void activateUser_valid() {
        given()
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .post("/api/user/" + validUserId + "/activate")

            .then()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    @Order(3)
    void activateUser_wrongRole() {
        given()
            .header("Authorization", "Bearer " + userToken)
            .port(randomServerPort)

            .when()
            .post("/api/user/" + validUserId + "/activate")

            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @Order(3)
    void activateUser_invalidId() {
        given()
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .post("/api/user/" + "XY" + "/activate")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Order(3)
    void activateUser_wrongId() {
        given()
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .post("/api/user/" + "20498590438" + "/activate")

            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
