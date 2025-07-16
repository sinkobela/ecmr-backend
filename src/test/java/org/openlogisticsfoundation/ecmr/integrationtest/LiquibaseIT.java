/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.integrationtest;

import java.sql.SQLException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import liquibase.exception.LiquibaseException;

public class LiquibaseIT extends AbstractIntegrationTest {
    @Test
    @Disabled
    void rollbackAndUpdateLiquibase() throws SQLException, LiquibaseException {
        this.rollbackLiquibase();
        this.updateLiquibase();
    }
}
