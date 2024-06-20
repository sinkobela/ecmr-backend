/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.persistence.entities;

import java.time.Instant;
import java.util.List;

import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ECMR_DATA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EcmrDataEntity extends BaseEntity {
    private String ecmrId;
    @Valid
    @JoinColumn(name = "sender_id")
    @OneToOne(cascade = CascadeType.ALL)
    private SenderEntity sender;
    @Valid
    @JoinColumn(name = "carrier_id")
    @OneToOne(cascade = CascadeType.ALL)
    private CarrierEntity carrier;
    @Valid
    @JoinColumn(name = "consignee_id")
    @OneToOne(cascade = CascadeType.ALL)
    private ConsigneeEntity consignee;
    @Valid
    @JoinColumn(name = "successive_carrier_id")
    @OneToOne(cascade = CascadeType.ALL)
    private SuccessiveCarrierEntity successiveCarrier;
    @Valid
    @OneToMany(mappedBy = "ecmrData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemEntity> itemList;
    @Valid
    @JoinColumn(name = "taking_over_the_goods_id")
    @OneToOne(cascade = CascadeType.ALL)
    private TakingOverTheGoodsEntity takingOverTheGoods;
    @Valid
    @JoinColumn(name = "delivery_of_the_goods_id")
    @OneToOne(cascade = CascadeType.ALL)
    private DeliveryOfTheGoodsEntity deliveryOfTheGoodsEntity;
    private String carrierReservationsObservations;
    //Transport Instructions Description
    private String sendersInstructions;
    //Documents handed to Carrier
    private String documentsRemarks;
    //Special Agreements between Sender and Carrier
    private String customSpecialAgreement;
    @Valid
    @JoinColumn(name = "to_be_paid_by")
    @OneToOne(cascade = CascadeType.ALL)
    private ToBePaidByEntity toBePaidBy;
    //Other Useful Particulars
    private String customParticulars;
    //Cash on Delivery
    private Integer customCashOnDelivery;
    private Instant customEstablishedDate;
    private String customEstablishedIn;
    //Non-Contractual Part reserved for the Carrier
    private String nonContractualCarrierRemarks;
    private String referenceIdentificationNumber;
    //Senders Instructions
    private String transportInstructionsDescription;
    @Valid
    private EcmrType ecmrType;
}
