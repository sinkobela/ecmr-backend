/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.models.commands;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EcmrCommand {
    @Valid
    private SenderInformationCommand senderInformation;

    @Valid
    private TakingOverTheGoodsCommand takingOverTheGoods;

    @Size(min = 2, max = 512)
    private String transportInstructionsDescription;

    @Valid
    private CarrierInformationCommand carrierInformation;

    @Valid
    private SuccessiveCarrierInformationCommand successiveCarrierInformation;

    private Boolean isMultiConsigneeShipment;

    @Valid
    private ConsigneeInformationCommand consigneeInformation;

    @Size(min = 2, max = 512)
    private String carrierReservationsObservations;

    @Size(min = 2, max = 512)
    private String documentsRemarks;

    @Valid
    private DeliveryOfTheGoodsCommand deliveryOfTheGoods;

    @Valid
    private List<ItemCommand> itemList;

    @Size(min = 2, max = 255)
    private String customSpecialAgreement;

    @Valid
    private ToBePaidByCommand toBePaidBy;

    @Size(min = 2, max = 512)
    private String customParticulars;

    @Min(1)
    @Max(999_999)
    private Integer customCashOnDelivery;

    private Instant customEstablishedDate;

    @Size(min = 2, max = 30)
    private String customEstablishedIn;

    @Size(min = 2, max = 512)
    private String nonContractualCarrierRemarks;

    @Valid
    private GoodsReceivedCommand goodsReceived;

    @Size(min = 1, max = 35)
    private String referenceIdentificationNumber;

    private String originUrl;
}
