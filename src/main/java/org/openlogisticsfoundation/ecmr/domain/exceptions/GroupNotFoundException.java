/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.exceptions;

public class GroupNotFoundException extends Exception {
    public GroupNotFoundException(Long id) {
        super("Group with id " + id + " not found");
    }

}
