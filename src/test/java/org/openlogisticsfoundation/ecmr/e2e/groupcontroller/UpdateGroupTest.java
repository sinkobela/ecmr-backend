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

public class UpdateGroupTest extends E2EBaseTest {

    static Long validGroupId;
    static Long childGroupId;

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

    @Test
    @Order(2)
    void createSecondGroup_validAllParams() {
        Response response = given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                    {
                      "name": "new group",
                      "description": "description",
                      "parentId": "%s"
                    }
                    """.formatted(validGroupId
                )
            )
            .port(randomServerPort)

            .when()
            .post("/api/group")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract().response();

        Group group = response.as(Group.class);
        childGroupId = group.getId();
    }

    // Update group
    @Test
    void updateGroup_valid() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {
                  "name": "new group",
                  "description": "description"
                }
                """
            )
            .port(randomServerPort)

            .when()
            .post("/api/group/" + validGroupId)

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void updateGroup_wrongRole() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + userToken)
            .body("""
                {
                  "name": "new group",
                  "description": "description"
                }
                """
            )
            .port(randomServerPort)

            .when()
            .post("/api/group/" + validGroupId)

            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void updateGroup_validRequiredParams() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {
                  "name": "new group"
                }
                """
            )
            .port(randomServerPort)

            .when()
            .post("/api/group/" + validGroupId)

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void updateGroup_noBody() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .post("/api/group/" + validGroupId)

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateGroup_invalidId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {
                  "name": "new group",
                  "description": "description"
                }
                """
            )
            .port(randomServerPort)

            .when()
            .post("/api/group/" + "xyz")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateGroup_wrongId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {
                  "name": "new group",
                  "description": "description"
                }
                """
            )
            .port(randomServerPort)

            .when()
            .post("/api/group/" + "21349849032")

            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    // Update parent group
    @Test
    void updateParentGroup_valid() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {
                  "parentId": 1
                }
                """
            )
            .port(randomServerPort)

            .when()
            .post("/api/group/" + childGroupId + "/update-parent")

            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void updateParentGroup_invalidId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {
                  "parentId": 1
                }
                """
            )
            .port(randomServerPort)

            .when()
            .post("/api/group/" + "xyz" + "/update-parent")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateParentGroup_wrongId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {
                  "parentId": 1
                }
                """
            )
            .port(randomServerPort)

            .when()
            .post("/api/group/" + "123421" + "/update-parent")

            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void updateParentGroup_noBody() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .port(randomServerPort)

            .when()
            .post("/api/group/" + childGroupId + "/update-parent")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateParentGroup_wrongParentId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {
                  "parentId": 12312
                }
                """
            )
            .port(randomServerPort)

            .when()
            .post("/api/group/" + childGroupId + "/update-parent")

            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void updateParentGroup_invalidParentId() {
        given()
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {
                  "parentId": "xyz"
                }
                """
            )
            .port(randomServerPort)

            .when()
            .post("/api/group/" + childGroupId + "/update-parent")

            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

}
