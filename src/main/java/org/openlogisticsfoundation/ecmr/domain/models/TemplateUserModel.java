/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.models;

import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;

import lombok.Data;

@Data
public class TemplateUserModel {
    private Long id;
    private Integer templateUserNumber;
    private String name;
    private EcmrModel ecmr;
}
