/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.e2e.groupcontroller;

import io.restassured.response.Response;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.e2e.E2EBaseTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RetrieveGroupsTest extends E2EBaseTest {

    static Long validGroupId;

    // Get all groups
    @Test
    @Order(1)
    void getAllGroups_validOwnGroups() {
        Response response = given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("currentUserGroupsOnly", "true")
            .port(randomServerPort)

            .when()
            .get("/api/group")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("size()", is(1))
            .extract().response();

        List<Group> groups = response.jsonPath().getList("", Group.class);
        validGroupId = groups.getFirst().getId();
    }

    @Test
    void getAllGroups_wrongRole() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + userToken)
            .queryParam("currentUserGroupsOnly", "true")
            .port(randomServerPort)

            .when()
            .get("/api/group")

            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());

    }

    @Test
    void getAllGroups_validAllGroups() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("currentUserGroupsOnly", "false")
            .port(randomServerPort)

            .when()
            .get("/api/group")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("size()", is(1));
    }

    @Test
    void getAllGroups_invalid() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("currentUserGroupsOnly", "xyz")
            .port(randomServerPort)

            .when()
            .get("/api/group")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    // Get flat list
    @Test
    void getAllGroupsFlat_validOwnGroups() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("currentUserGroupsOnly", "true")
            .port(randomServerPort)

            .when()
            .get("/api/group/flat-list")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("size()", is(2));
    }

    @Test
    void getAllGroupsFlat_wrongUser() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + userToken)
            .queryParam("currentUserGroupsOnly", "true")
            .port(randomServerPort)

            .when()
            .get("/api/group/flat-list")

            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void getAllGroupsFlat_validAllGroups() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("currentUserGroupsOnly", "false")
            .port(randomServerPort)

            .when()
            .get("/api/group/flat-list")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("size()", is(2));
    }

    @Test
    void getAllGroupsFlat_invalid() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .queryParam("currentUserGroupsOnly", "xyz")
            .port(randomServerPort)

            .when()
            .get("/api/group/flat-list")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    // get specific group
    @Test
    void getSpecificGroup_valid() {
        Response response = given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/group/" + validGroupId)

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract().response();

        Group group = response.jsonPath().getObject("", Group.class);
        assertEquals(group.getId(), validGroupId);
    }

    @Test
    void getSpecificGroup_wrongRole() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + userToken)
            .port(randomServerPort)

            .when()
            .get("/api/group/" + validGroupId)

            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void getSpecificGroup_wrongId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/group/" + "123589032")

            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void getSpecificGroup_invalidId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/group/" + "xyz")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    // Get users for group
    @Test
    @Order(2)
    void getUsersForGroup_valid() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/group/" + validGroupId + "/users")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("size()", is(2));
    }

    @Test
    @Order(2)
    void getUsersForGroup_wrongRole() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + userToken)
            .port(randomServerPort)

            .when()
            .get("/api/group/" + validGroupId + "/users")

            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void getUsersForGroup_invalidId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/group/" + "xyz" + "/users")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void getUsersForGroup_wrongId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .get("/api/group/" + "1234423" + "/users")

            .then()
            .statusCode(HttpStatus.OK.value())
            .body("size()", is(0))
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

}
