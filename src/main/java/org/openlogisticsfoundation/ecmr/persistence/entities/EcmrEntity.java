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
import java.util.UUID;

import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ECMR", indexes = {
        @Index(columnList = "sender_information_id"),
        @Index(columnList = "carrier_information_id"),
        @Index(columnList = "consignee_information_id"),
        @Index(columnList = "successive_carrier_information_id"),
        @Index(columnList = "taking_over_the_goods_id"),
        @Index(columnList = "delivery_of_the_goods_id"),
        @Index(columnList = "to_be_paid_by")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(name = "Ecmr.all",
        attributeNodes = {
                @NamedAttributeNode("deliveryOfTheGoods"),
                @NamedAttributeNode("takingOverTheGoods"),
                @NamedAttributeNode("goodsReceived"),
                @NamedAttributeNode(value = "senderInformation", subgraph = "senderInformation.all"),
                @NamedAttributeNode(value = "carrierInformation", subgraph = "carrierInformation.all"),
                @NamedAttributeNode(value = "consigneeInformation", subgraph = "consigneeInformation.all"),
                @NamedAttributeNode(value = "successiveCarrierInformation", subgraph = "successiveCarrierInformation.all"),
                @NamedAttributeNode("itemList"),
                @NamedAttributeNode(value = "toBePaidBy", subgraph = "toBePaidBy.all"),
        }, subgraphs = {
        @NamedSubgraph(name = "toBePaidBy.all", attributeNodes = {
                @NamedAttributeNode("customChargeCarriage"),
                @NamedAttributeNode("customChargeCustomsDuties"),
                @NamedAttributeNode("customChargeOther"),
                @NamedAttributeNode("customChargeSupplementary"),
        }),
        @NamedSubgraph(name = "senderInformation.all", attributeNodes = {
                @NamedAttributeNode("signature")
        }),
        @NamedSubgraph(name = "consigneeInformation.all", attributeNodes = {
                @NamedAttributeNode("signature")
        }),
        @NamedSubgraph(name = "successiveCarrierInformation.all", attributeNodes = {
                @NamedAttributeNode("signature")
        }),
        @NamedSubgraph(name = "carrierInformation.all", attributeNodes = {
                @NamedAttributeNode("signature")
        })
})
public class EcmrEntity extends BaseEntity {
    @NotNull
    private UUID ecmrId = UUID.randomUUID();
    @Valid
    @JoinColumn(name = "sender_information_id")
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private SenderInformationEntity senderInformation;
    @Valid
    @JoinColumn(name = "carrier_information_id")
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private CarrierInformationEntity carrierInformation;
    @Valid
    @JoinColumn(name = "consignee_information_id")
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private ConsigneeInformationEntity consigneeInformation;
    @Valid
    @JoinColumn(name = "successive_carrier_information_id")
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private SuccessiveCarrierInformationEntity successiveCarrierInformation;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ECMR_ID")
    private List<ItemEntity> itemList;
    @Valid
    @JoinColumn(name = "taking_over_the_goods_id")
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private TakingOverTheGoodsEntity takingOverTheGoods;
    @Valid
    @JoinColumn(name = "goods_received_id")
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private GoodsReceivedEntity goodsReceived;
    @Valid
    @JoinColumn(name = "delivery_of_the_goods_id")
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private DeliveryOfTheGoodsEntity deliveryOfTheGoods;
    private String carrierReservationsObservations;
    //Documents handed to Carrier
    private String documentsRemarks;
    //Special Agreements between Sender and Carrier
    private String customSpecialAgreement;
    @Valid
    @JoinColumn(name = "to_be_paid_by")
    @OneToOne(cascade = CascadeType.ALL, optional = false)
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
    private EcmrType type;
    private EcmrStatus ecmrStatus;
    private Instant createdAt;
    private String createdBy;
    private Instant editedAt;
    private String editedBy;
    private String shareWithSenderToken;
    private String shareWithCarrierToken;
    private String shareWithConsigneeToken;
    private String shareWithReaderToken;

    @OneToOne(mappedBy = "ecmr")
    @JsonManagedReference
    private TemplateUserEntity template;
}
