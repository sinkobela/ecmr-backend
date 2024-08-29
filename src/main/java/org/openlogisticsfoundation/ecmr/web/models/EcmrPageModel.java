/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.models;

import java.util.List;

import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EcmrPageModel {
    private int totalPages;
    private long totalElements;
    private List<EcmrModel> ecmrs;
}
