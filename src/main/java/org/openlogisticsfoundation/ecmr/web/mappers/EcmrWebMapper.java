/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.web.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.areas.one.SenderInformation;
import org.openlogisticsfoundation.ecmr.api.model.areas.seven.SuccessiveCarrierInformation;
import org.openlogisticsfoundation.ecmr.api.model.areas.six.CarrierInformation;
import org.openlogisticsfoundation.ecmr.api.model.areas.twentyfour.GoodsReceived;
import org.openlogisticsfoundation.ecmr.api.model.areas.two.ConsigneeInformation;
import org.openlogisticsfoundation.ecmr.api.model.areas.two.MultiConsigneeShipment;
import org.openlogisticsfoundation.ecmr.api.model.compositions.Item;
import org.openlogisticsfoundation.ecmr.domain.models.commands.*;
import org.openlogisticsfoundation.ecmr.web.models.FilterRequestModel;
import org.openlogisticsfoundation.ecmr.web.models.SealModel;
import org.openlogisticsfoundation.ecmr.web.models.SharedCarrierInformationModel;
import org.openlogisticsfoundation.ecmr.web.models.SignModel;

@Mapper(componentModel = "spring")
public interface EcmrWebMapper {

    @Mapping(source = "ecmrConsignment.sendersInstructions.transportInstructionsDescription", target = "transportInstructionsDescription")
    @Mapping(source = "ecmrConsignment.carriersReservationsAndObservationsOnTakingOverTheGoods.carrierReservationsObservations", target = "carrierReservationsObservations")
    @Mapping(source = "ecmrConsignment.specialAgreementsSenderCarrier.customSpecialAgreement", target = "customSpecialAgreement")
    @Mapping(source = "ecmrConsignment.otherUsefulParticulars.customParticulars", target = "customParticulars")
    @Mapping(source = "ecmrConsignment.cashOnDelivery.customCashOnDelivery", target = "customCashOnDelivery")
    @Mapping(source = "ecmrConsignment.established.customEstablishedIn", target = "customEstablishedIn")
    @Mapping(source = "ecmrConsignment.established.customEstablishedDate", target = "customEstablishedDate")
    @Mapping(source = "ecmrConsignment.nonContractualPartReservedForTheCarrier.nonContractualCarrierRemarks", target = "nonContractualCarrierRemarks")
    @Mapping(source = "ecmrConsignment.referenceIdentificationNumber.value", target = "referenceIdentificationNumber")
    @Mapping(source = "ecmrConsignment.documentsHandedToCarrier.documentsRemarks", target = "documentsRemarks")
    @Mapping(source = "ecmrConsignment.deliveryOfTheGoods", target = "deliveryOfTheGoods")
    @Mapping(source = "ecmrConsignment.senderInformation", target = "senderInformation")
    @Mapping(target = "isMultiConsigneeShipment", expression = "java(mapIsMultiConsigneeShipment(ecmrModel.getEcmrConsignment().getMultiConsigneeShipment()))")
    @Mapping(source = "ecmrConsignment.consigneeInformation", target = "consigneeInformation")
    @Mapping(source = "ecmrConsignment.takingOverTheGoods", target = "takingOverTheGoods")
    @Mapping(source = "ecmrConsignment.carrierInformation", target = "carrierInformation")
    @Mapping(source = "ecmrConsignment.successiveCarrierInformation", target = "successiveCarrierInformation")
    @Mapping(source = "ecmrConsignment.itemList", target = "itemList")
    @Mapping(source = "ecmrConsignment.toBePaidBy", target = "toBePaidBy")
    @Mapping(source = "ecmrConsignment.goodsReceived", target = "goodsReceived")
    @Mapping(source = "originUrl", target = "originUrl")
    EcmrCommand toCommand(EcmrModel ecmrModel);

    @Mapping(source = "senderCountryCode.value", target = "countryCode")
    @Mapping(source = "senderContactInformation.email", target = "email")
    @Mapping(source = "senderContactInformation.phone", target = "phone")
    @Mapping(source = "senderCity", target = "city")
    @Mapping(source = "senderNameCompany", target = "nameCompany")
    @Mapping(source = "senderNamePerson", target = "namePerson")
    @Mapping(source = "senderPostcode", target = "postcode")
    @Mapping(source = "senderStreet", target = "street")
    SenderInformationCommand map(SenderInformation value);

    @Mapping(source = "consigneeCountryCode.value", target = "countryCode")
    @Mapping(source = "consigneeContactInformation.email", target = "email")
    @Mapping(source = "consigneeContactInformation.phone", target = "phone")
    @Mapping(source = "consigneeCity", target = "city")
    @Mapping(source = "consigneeNameCompany", target = "nameCompany")
    @Mapping(source = "consigneeNamePerson", target = "namePerson")
    @Mapping(source = "consigneePostcode", target = "postcode")
    @Mapping(source = "consigneeStreet", target = "street")
    ConsigneeInformationCommand map(ConsigneeInformation value);

    @Mapping(source = "carrierCountryCode.value", target = "countryCode")
    @Mapping(source = "carrierContactInformation.email", target = "email")
    @Mapping(source = "carrierContactInformation.phone", target = "phone")
    @Mapping(source = "carrierCity", target = "city")
    @Mapping(source = "carrierNameCompany", target = "nameCompany")
    @Mapping(source = "carrierNamePerson", target = "namePerson")
    @Mapping(source = "carrierPostcode", target = "postcode")
    @Mapping(source = "carrierStreet", target = "street")
    @Mapping(source = "carrierLicensePlate", target = "carrierLicensePlate")
    CarrierInformationCommand map(CarrierInformation value);

    @Mapping(source = "successiveCarrierCountryCode.value", target = "countryCode")
    @Mapping(source = "successiveCarrierContactInformation.email", target = "email")
    @Mapping(source = "successiveCarrierContactInformation.phone", target = "phone")
    @Mapping(source = "successiveCarrierCity", target = "city")
    @Mapping(source = "successiveCarrierNameCompany", target = "nameCompany")
    @Mapping(source = "successiveCarrierNamePerson", target = "namePerson")
    @Mapping(source = "successiveCarrierPostcode", target = "postcode")
    @Mapping(source = "successiveCarrierStreet", target = "street")
    SuccessiveCarrierInformationCommand map(SuccessiveCarrierInformation value);

    GoodsReceivedCommand map(GoodsReceived value);

    @Mapping(source = "marksAndNos.logisticsShippingMarksMarking", target = "logisticsShippingMarksMarking")
    @Mapping(source = "marksAndNos.logisticsShippingMarksCustomBarcode", target = "logisticsShippingMarksCustomBarcode")
    @Mapping(source = "numberOfPackages.logisticsPackageItemQuantity", target = "logisticsPackageItemQuantity")
    @Mapping(source = "methodOfPacking.logisticsPackageType", target = "logisticsPackageType")
    @Mapping(source = "natureOfTheGoods.transportCargoIdentification", target = "transportCargoIdentification")
    @Mapping(source = "grossWeightInKg.supplyChainConsignmentItemGrossWeight", target = "supplyChainConsignmentItemGrossWeight")
    @Mapping(source = "volumeInM3.supplyChainConsignmentItemGrossVolume", target = "supplyChainConsignmentItemGrossVolume")
    ItemCommand map(Item value);

    SignCommand map(SignModel model);

    SealCommand map(SealModel model);

    FilterRequestCommand map(FilterRequestModel model);

    @Mapping(source = "carrierNameCompany", target = "carrierNameCompany")
    @Mapping(source = "carrierNamePerson", target = "carrierNamePerson")
    SharedCarrierInformationModel toSharedCarrierInformation(CarrierInformation value);

    /**
     * Checks whether ECMR has multiple consignees
     *
     * @param multiConsigneeShipment The MultiConsigneeShipment field of the ECMR Model
     * @return True if the ECMR is marked as a multi consignee shipment, otherwise false
     *
     */
    default Boolean mapIsMultiConsigneeShipment(MultiConsigneeShipment multiConsigneeShipment) {
        return multiConsigneeShipment != null
            && multiConsigneeShipment.getIsMultiConsigneeShipment() != null
            && multiConsigneeShipment.getIsMultiConsigneeShipment();
    }
}
