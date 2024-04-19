/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.integrationtest;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import javax.sql.DataSource;

import org.openlogisticsfoundation.ecmr.EcmrBackendApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringResourceAccessor;
import lombok.extern.log4j.Log4j2;

@SpringBootTest(
        classes = EcmrBackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://dummy.org/"
                //                "spring.datasource.url=${DB_URL}",
                //                "spring.datasource.username=${DB_USERNAME}",
                //                "spring.datasource.password=${DB_PASSWORD}"
        }
)
@AutoConfigureMockMvc
@Log4j2
@Sql(value = { "/clear-all-tables.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Sql(value = { "/test-data.sql" })
public class AbstractIntegrationTest {
    @Autowired
    protected MockMvc mvc;

    @Autowired
    DataSource dataSource;

    @Autowired
    ResourceLoader resourceLoader;

    protected void rollbackLiquibase() throws SQLException, LiquibaseException {
        getLiquibase().rollback(Date.valueOf(LocalDate.of(2020, 1, 1)), "");
    }

    protected void updateLiquibase() throws SQLException, LiquibaseException {
        List<ChangeSet> changeSets = getLiquibase().listUnrunChangeSets((Contexts) null, new LabelExpression());
        log.info("Trying to apply " + changeSets.size() + " changeset");
        getLiquibase().update();
        log.info("All changesets applied");
    }

    private Liquibase getLiquibase() throws SQLException, DatabaseException {
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(
                        new JdbcConnection(dataSource.getConnection())
                );
        Liquibase liquibase = new Liquibase(
                "db/db.changelog-main.xml",
                new SpringResourceAccessor(resourceLoader),
                database
        );
        return liquibase;
    }

    protected void mockJwtAuthentication(String email) {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("email", email)
                .build();
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt);
        authenticationToken.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
