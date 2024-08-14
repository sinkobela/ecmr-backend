/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.models;

import java.util.List;

import lombok.Data;

@Data
public class Group {
    private Long id;
    private String name;
    private String description;
    private List<Group> children;
}
