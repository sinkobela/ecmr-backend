/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.persistence.entities;

public interface SealedDocumentProjection {
    Long getId();
    EcmrSealEntity getSenderSeal();
    EcmrSealEntity getCarrierSeal();
    EcmrSealEntity getSuccessiveCarrierSeal();
    EcmrSealEntity getConsigneeSeal();
}
