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

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeleteGroupTest extends E2EBaseTest {

    static Long validGroupId;

    // Preparation
    @Test
    @Order(1)
    void createGroup_validAllParams() {
        Response response = given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {
                  "name": "new group",
                  "description": "description",
                  "parentId": "1"
                }
                """
            )
            .port(randomServerPort)

            .when()
            .post("/api/group")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract().response();

        Group group = response.as(Group.class);
        validGroupId = group.getId();
    }

    // Delete Group
    @Test
    void deleteGroup_valid() {
        Response response = given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .delete("/api/group/" + validGroupId)

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract().response();

        assertTrue(response.as(Boolean.class));
    }

    @Test
    void deleteGroup_invalidId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .delete("/api/group/" + "xyz")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void deleteGroup_wrongId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .delete("/api/group/" + "403294")

            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

}
