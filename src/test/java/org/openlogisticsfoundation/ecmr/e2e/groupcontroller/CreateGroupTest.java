/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.e2e.groupcontroller;

import org.junit.jupiter.api.Test;
import org.openlogisticsfoundation.ecmr.e2e.E2EBaseTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;

public class CreateGroupTest extends E2EBaseTest {

    @Test
    void createGroup_validAllParams() {
        given()
            .accept(MediaType.APPLICATION_JSON)
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

            .when()
            .post("/api/group")

            .then()
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void createGroup_wrongRole() {
        given()
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + userToken)
            .body("""
                {
                  "name": "new group",
                  "description": "description",
                  "parentId": "1"
                }
                """
            )

            .when()
            .post("/api/group")

            .then()
            .status(HttpStatus.FORBIDDEN);
    }

    @Test
    void createGroup_validRequiredParams() {
        given()
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {
                  "name": "new group",
                  "parentId": "1"
                }
                """
            )

            .when()
            .post("/api/group")

            .then()
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void createGroup_noParentId() {
        given()
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {
                  "name": "new group"
                }
                """
            )

            .when()
            .post("/api/group")

            .then()
            .status(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createGroup_invalidParentId() {
        given()
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)
            .body("""
                {
                  "name": "new group",
                  "parentId": "100"
                }
                """
            )

            .when()
            .post("/api/group")

            .then()
            .status(HttpStatus.NOT_FOUND);
    }

    @Test
    void createGroup_noBody() {
        given()
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + adminToken)

            .when()
            .post("/api/group")

            .then()
            .status(HttpStatus.BAD_REQUEST);
    }

}
