/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "CARRIER_INFORMATION", indexes = {
        @Index(columnList = "signature_id"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarrierInformationEntity extends EcmrMemberEntity {
    private String carrierLicensePlate;
}
