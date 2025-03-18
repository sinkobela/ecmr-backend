/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openlogisticsfoundation.ecmr.api.model.SealedDocument;

@AllArgsConstructor
@Getter
public class EcmrExportResult {
    SealedDocument sealedDocument;
    EcmrRole ecmrRole;
}
