/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.models;

import org.openlogisticsfoundation.ecmr.api.model.EcmrSeal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SealedDocumentWithoutEcmr {
    private EcmrSeal senderSeal;
    private EcmrSeal carrierSeal;
    private EcmrSeal successiveCarrierSeal;
    private EcmrSeal consigneeSeal;
}
