/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.e2e;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.*;
import org.openlogisticsfoundation.ecmr.EcmrBackendApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = EcmrBackendApplication.class)
@TestPropertySource("classpath:application-e2e-test.properties")
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class E2EBaseTest {

    static KeycloakContainer keycloak = new KeycloakContainer()
        .withExposedPorts(9000, 8080)
        .withRealmImportFile("keycloak-config.json")
        .withReuse(true);

    @LocalServerPort
    protected int randomServerPort;

    static protected String adminToken = "";
    static protected String userToken = "";

    @DynamicPropertySource
    static void jwtValidationProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
            () -> keycloak.getAuthServerUrl() + "/realms/test");
    }

    @BeforeAll
    static void setup() {
        keycloak.start();
    }

    @BeforeEach
    void setUp() {
        RestAssuredWebTestClient.webTestClient(
            WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + randomServerPort)
                .build()
        );

        adminToken = RestAssured.given()
            .contentType("application/x-www-form-urlencoded; charset=utf-8")
            .formParam("grant_type", "password")
            .formParam("client_id", "testClient")
            .formParam("username", "test-admin")
            .formParam("password", "password")
            .when()
            .post(keycloak.getAuthServerUrl() + "/realms/" + "test" + "/protocol/openid-connect/token")
            .then()
            .extract()
            .path("access_token");

        userToken = RestAssured.given()
            .contentType("application/x-www-form-urlencoded; charset=utf-8")
            .formParam("grant_type", "password")
            .formParam("client_id", "testClient")
            .formParam("username", "test-user")
            .formParam("password", "password")
            .when()
            .post(keycloak.getAuthServerUrl() + "/realms/" + "test" + "/protocol/openid-connect/token")
            .then()
            .extract()
            .path("access_token");
    }
}

