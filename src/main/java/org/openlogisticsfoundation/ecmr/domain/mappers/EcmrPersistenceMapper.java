/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.api.model.areas.four.DeliveryOfTheGoods;
import org.openlogisticsfoundation.ecmr.api.model.areas.one.SenderInformation;
import org.openlogisticsfoundation.ecmr.api.model.areas.seven.SuccessiveCarrierInformation;
import org.openlogisticsfoundation.ecmr.api.model.areas.seventeen.CustomCharge;
import org.openlogisticsfoundation.ecmr.api.model.areas.seventeen.ToBePaidBy;
import org.openlogisticsfoundation.ecmr.api.model.areas.six.CarrierInformation;
import org.openlogisticsfoundation.ecmr.api.model.areas.ten.LogisticsShippingMarksCustomBarcode;
import org.openlogisticsfoundation.ecmr.api.model.areas.three.TakingOverTheGoods;
import org.openlogisticsfoundation.ecmr.api.model.areas.twentyfour.GoodsReceived;
import org.openlogisticsfoundation.ecmr.api.model.areas.two.ConsigneeInformation;
import org.openlogisticsfoundation.ecmr.api.model.compositions.Item;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.commands.CustomChargeCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ItemCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.LogisticsShippingMarksCustomBarcodeCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.CarrierInformationEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.ConsigneeInformationEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.CustomChargeEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.DeliveryOfTheGoodsEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.GoodsReceivedEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.ItemEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.LogisticsShippingMarksCustomBarcodeEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.SenderInformationEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.SuccessiveCarrierInformationEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.TakingOverTheGoodsEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.ToBePaidByEntity;

@Mapper(componentModel = "spring")
public interface EcmrPersistenceMapper {

    @Mapping(target = "senderInformation.id", ignore = true)
    @Mapping(target = "carrierInformation.id", ignore = true)
    @Mapping(target = "consigneeInformation.id", ignore = true)
    @Mapping(target = "successiveCarrierInformation.id", ignore = true)
    @Mapping(target = "deliveryOfTheGoods.id", ignore = true)
    @Mapping(target = "takingOverTheGoods.id", ignore = true)
    @Mapping(target = "goodsReceived.id", ignore = true)
    @Mapping(target = "toBePaidBy.id", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ecmrId", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "editedBy", ignore = true)
    @Mapping(target = "editedAt", ignore = true)
    @Mapping(target = "shareWithSenderToken", ignore = true)
    @Mapping(target = "shareWithCarrierToken", ignore = true)
    @Mapping(target = "shareWithConsigneeToken", ignore = true)
    @Mapping(target = "shareWithReaderToken", ignore = true)
    EcmrEntity toEntity(EcmrCommand ecmrModel, EcmrType type, EcmrStatus ecmrStatus);

    @Mapping(target = "id", ignore = true)
    ItemEntity map(ItemCommand value);

    @Mapping(target = "id", ignore = true)
    LogisticsShippingMarksCustomBarcodeEntity map(LogisticsShippingMarksCustomBarcodeCommand value);

    @Mapping(target = "id", ignore = true)
    CustomChargeEntity map(CustomChargeCommand value);

    @Mapping(target = "ecmrId", source = "ecmrId")
    @Mapping(target = "ecmrStatus", source = "ecmrStatus")
    @Mapping(target = "ecmrConsignment.senderInformation", source = "senderInformation")
    @Mapping(target = "ecmrConsignment.multiConsigneeShipment.isMultiConsigneeShipment", source = "isMultiConsigneeShipment")
    @Mapping(target = "ecmrConsignment.consigneeInformation", source = "consigneeInformation")
    @Mapping(target = "ecmrConsignment.deliveryOfTheGoods", source = "deliveryOfTheGoods")
    @Mapping(target = "ecmrConsignment.sendersInstructions.transportInstructionsDescription", source = "transportInstructionsDescription")
    @Mapping(target = "ecmrConsignment.carrierInformation", source = "carrierInformation")
    @Mapping(target = "ecmrConsignment.successiveCarrierInformation", source = "successiveCarrierInformation")
    @Mapping(target = "ecmrConsignment.carriersReservationsAndObservationsOnTakingOverTheGoods.carrierReservationsObservations", source = "carrierReservationsObservations")
    @Mapping(target = "ecmrConsignment.takingOverTheGoods", source = "takingOverTheGoods")
    @Mapping(target = "ecmrConsignment.documentsHandedToCarrier.documentsRemarks", source = "documentsRemarks")
    @Mapping(target = "ecmrConsignment.itemList", source = "itemList")
    @Mapping(target = "ecmrConsignment.specialAgreementsSenderCarrier.customSpecialAgreement", source = "customSpecialAgreement")
    @Mapping(target = "ecmrConsignment.toBePaidBy", source = "toBePaidBy")
    @Mapping(target = "ecmrConsignment.otherUsefulParticulars.customParticulars", source = "customParticulars")
    @Mapping(target = "ecmrConsignment.cashOnDelivery.customCashOnDelivery", source = "customCashOnDelivery")
    @Mapping(target = "ecmrConsignment.established.customEstablishedDate", source = "customEstablishedDate")
    @Mapping(target = "ecmrConsignment.established.customEstablishedIn", source = "customEstablishedIn")
    @Mapping(target = "ecmrConsignment.goodsReceived", source = "goodsReceived")
    @Mapping(target = "ecmrConsignment.nonContractualPartReservedForTheCarrier.nonContractualCarrierRemarks", source = "nonContractualCarrierRemarks")
    @Mapping(target = "ecmrConsignment.referenceIdentificationNumber.value", source = "referenceIdentificationNumber")
    EcmrModel toModel(EcmrEntity value);

    @Mapping(target = "confirmedLogisticsLocationName", source = "confirmedLogisticsLocationName")
    @Mapping(target = "consigneeReservationsObservations", source = "consigneeReservationsObservations")
    @Mapping(target = "consigneeTimeOfArrival", source = "consigneeTimeOfArrival")
    @Mapping(target = "consigneeTimeOfDeparture", source = "consigneeTimeOfDeparture")
    @Mapping(target = "consigneeSignatureDate", source = "consigneeSignatureDate")
    @Mapping(target = "consigneeSignature", ignore = true)
    GoodsReceived toGoodsReceived(GoodsReceivedEntity goodsReceivedEntity);

    @Mapping(source = "street", target = "senderStreet")
    @Mapping(source = "personName", target = "senderPersonName")
    @Mapping(source = "companyName", target = "senderCompanyName")
    @Mapping(source = "postcode", target = "senderPostcode")
    @Mapping(source = "countryCode", target = "senderCountryCode.value")
    @Mapping(source = "city", target = "senderCity")
    @Mapping(source = "email", target = "senderContactInformation.email")
    @Mapping(source = "phone", target = "senderContactInformation.phone")
    SenderInformation map(SenderInformationEntity value);

    @Mapping(source = "street", target = "consigneeStreet")
    @Mapping(source = "personName", target = "consigneePersonName")
    @Mapping(source = "companyName", target = "consigneeCompanyName")
    @Mapping(source = "postcode", target = "consigneePostcode")
    @Mapping(source = "countryCode", target = "consigneeCountryCode.value")
    @Mapping(source = "city", target = "consigneeCity")
    @Mapping(source = "email", target = "consigneeContactInformation.email")
    @Mapping(source = "phone", target = "consigneeContactInformation.phone")
    ConsigneeInformation map(ConsigneeInformationEntity value);

    @Mapping(source = "street", target = "carrierStreet")
    @Mapping(source = "personName", target = "carrierDriverName")
    @Mapping(source = "companyName", target = "carrierCompanyName")
    @Mapping(source = "postcode", target = "carrierPostcode")
    @Mapping(source = "countryCode", target = "carrierCountryCode.value")
    @Mapping(source = "city", target = "carrierCity")
    @Mapping(source = "email", target = "carrierContactInformation.email")
    @Mapping(source = "phone", target = "carrierContactInformation.carrierPhone")
    @Mapping(source = "driverPhone", target = "carrierContactInformation.driverPhone")
    @Mapping(source = "carrierLicensePlate", target = "carrierLicensePlate")
    CarrierInformation map(CarrierInformationEntity value);

    @Mapping(source = "street", target = "successiveCarrierStreet")
    @Mapping(source = "personName", target = "successiveCarrierDriverName")
    @Mapping(source = "companyName", target = "successiveCarrierCompanyName")
    @Mapping(source = "postcode", target = "successiveCarrierPostcode")
    @Mapping(source = "countryCode", target = "successiveCarrierCountryCode.value")
    @Mapping(source = "city", target = "successiveCarrierCity")
    @Mapping(source = "email", target = "successiveCarrierContactInformation.email")
    @Mapping(source = "driverPhone", target = "successiveCarrierContactInformation.driverPhone")
    @Mapping(source = "phone", target = "successiveCarrierContactInformation.carrierPhone")
    SuccessiveCarrierInformation map(SuccessiveCarrierInformationEntity value);

    @Mapping(source = "logisticsPackageItemQuantity", target = "numberOfPackages.logisticsPackageItemQuantity")
    @Mapping(source = "logisticsPackageType", target = "methodOfPacking.logisticsPackageType")
    @Mapping(source = "logisticsShippingMarksMarking", target = "marksAndNos.logisticsShippingMarksMarking")
    @Mapping(source = "logisticsShippingMarksCustomBarcodeList", target = "marksAndNos.logisticsShippingMarksCustomBarcodeList")
    @Mapping(source = "supplyChainConsignmentItemGrossVolume", target = "volumeInM3.supplyChainConsignmentItemGrossVolume")
    @Mapping(source = "supplyChainConsignmentItemGrossWeight", target = "grossWeightInKg.supplyChainConsignmentItemGrossWeight")
    @Mapping(source = "transportCargoIdentification", target = "natureOfTheGoods.transportCargoIdentification")
    Item map(ItemEntity value);

    @Mapping(target = "senderInformation.id", ignore = true)
    @Mapping(target = "carrierInformation.id", ignore = true)
    @Mapping(target = "consigneeInformation.id", ignore = true)
    @Mapping(target = "successiveCarrierInformation.id", ignore = true)
    @Mapping(target = "deliveryOfTheGoods.id", ignore = true)
    @Mapping(target = "takingOverTheGoods.id", ignore = true)
    @Mapping(target = "goodsReceived.id", ignore = true)
    @Mapping(target = "toBePaidBy.id", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ecmrId", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "editedBy", ignore = true)
    @Mapping(target = "editedAt", ignore = true)
    @Mapping(target = "shareWithSenderToken", ignore = true)
    @Mapping(target = "shareWithCarrierToken", ignore = true)
    @Mapping(target = "shareWithConsigneeToken", ignore = true)
    @Mapping(target = "shareWithReaderToken", ignore = true)
    @Mapping(target = "ecmrStatus", ignore = true)
    EcmrEntity toEntity(@MappingTarget EcmrEntity ecmrEntity, EcmrCommand ecmrCommand, EcmrType type);

    @Mapping(target = "itemList", source = "ecmrConsignment.itemList")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "editedBy", ignore = true)
    @Mapping(target = "editedAt", ignore = true)
    @Mapping(target = "shareWithSenderToken", ignore = true)
    @Mapping(target = "shareWithCarrierToken", ignore = true)
    @Mapping(target = "shareWithConsigneeToken", ignore = true)
    @Mapping(target = "shareWithReaderToken", ignore = true)
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
    @Mapping(source = "ecmrConsignment.multiConsigneeShipment.isMultiConsigneeShipment", target = "isMultiConsigneeShipment")
    @Mapping(source = "ecmrConsignment.consigneeInformation", target = "consigneeInformation")
    @Mapping(source = "ecmrConsignment.takingOverTheGoods", target = "takingOverTheGoods")
    @Mapping(source = "ecmrConsignment.carrierInformation", target = "carrierInformation")
    @Mapping(source = "ecmrConsignment.successiveCarrierInformation", target = "successiveCarrierInformation")
    @Mapping(source = "ecmrConsignment.toBePaidBy", target = "toBePaidBy")
    @Mapping(source = "ecmrConsignment.goodsReceived", target = "goodsReceived")
    @Mapping(target = "type", ignore = true)
    EcmrEntity toEntity(EcmrModel ecmrModel);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "senderCountryCode.value", target = "countryCode")
    @Mapping(source = "senderContactInformation.email", target = "email")
    @Mapping(source = "senderContactInformation.phone", target = "phone")
    @Mapping(source = "senderCity", target = "city")
    @Mapping(source = "senderCompanyName", target = "companyName")
    @Mapping(source = "senderPersonName", target = "personName")
    @Mapping(source = "senderPostcode", target = "postcode")
    @Mapping(source = "senderStreet", target = "street")
    SenderInformationEntity mapSenderInformation(SenderInformation value);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "consigneeCountryCode.value", target = "countryCode")
    @Mapping(source = "consigneeContactInformation.email", target = "email")
    @Mapping(source = "consigneeContactInformation.phone", target = "phone")
    @Mapping(source = "consigneeCity", target = "city")
    @Mapping(source = "consigneeCompanyName", target = "companyName")
    @Mapping(source = "consigneePersonName", target = "personName")
    @Mapping(source = "consigneePostcode", target = "postcode")
    @Mapping(source = "consigneeStreet", target = "street")
    ConsigneeInformationEntity map(ConsigneeInformation value);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "carrierCountryCode.value", target = "countryCode")
    @Mapping(source = "carrierContactInformation.email", target = "email")
    @Mapping(source = "carrierContactInformation.carrierPhone", target = "phone")
    @Mapping(source = "carrierContactInformation.driverPhone", target = "driverPhone")
    @Mapping(source = "carrierCity", target = "city")
    @Mapping(source = "carrierCompanyName", target = "companyName")
    @Mapping(source = "carrierDriverName", target = "personName")
    @Mapping(source = "carrierPostcode", target = "postcode")
    @Mapping(source = "carrierStreet", target = "street")
    @Mapping(source = "carrierLicensePlate", target = "carrierLicensePlate")
    CarrierInformationEntity map(CarrierInformation value);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "successiveCarrierCountryCode.value", target = "countryCode")
    @Mapping(source = "successiveCarrierContactInformation.email", target = "email")
    @Mapping(source = "successiveCarrierContactInformation.carrierPhone", target = "phone")
    @Mapping(source = "successiveCarrierContactInformation.driverPhone", target = "driverPhone")
    @Mapping(source = "successiveCarrierCity", target = "city")
    @Mapping(source = "successiveCarrierCompanyName", target = "companyName")
    @Mapping(source = "successiveCarrierDriverName", target = "personName")
    @Mapping(source = "successiveCarrierPostcode", target = "postcode")
    @Mapping(source = "successiveCarrierStreet", target = "street")
    SuccessiveCarrierInformationEntity map(SuccessiveCarrierInformation value);

    @Mapping(target = "id", ignore = true)
    GoodsReceivedEntity map(GoodsReceived value);

    @Mapping(target = "id", ignore = true)
    DeliveryOfTheGoodsEntity map(DeliveryOfTheGoods value);

    @Mapping(target = "id", ignore = true)
    TakingOverTheGoodsEntity map(TakingOverTheGoods value);

    @Mapping(target = "id", ignore = true)
    ToBePaidByEntity map(ToBePaidBy value);

    @Mapping(target = "id", ignore = true)
    CustomChargeEntity map(CustomCharge value);

    @Mapping(source = "marksAndNos.logisticsShippingMarksMarking", target = "logisticsShippingMarksMarking")
    @Mapping(source = "marksAndNos.logisticsShippingMarksCustomBarcodeList", target = "logisticsShippingMarksCustomBarcodeList")
    @Mapping(source = "numberOfPackages.logisticsPackageItemQuantity", target = "logisticsPackageItemQuantity")
    @Mapping(source = "methodOfPacking.logisticsPackageType", target = "logisticsPackageType")
    @Mapping(source = "natureOfTheGoods.transportCargoIdentification", target = "transportCargoIdentification")
    @Mapping(source = "grossWeightInKg.supplyChainConsignmentItemGrossWeight", target = "supplyChainConsignmentItemGrossWeight")
    @Mapping(source = "volumeInM3.supplyChainConsignmentItemGrossVolume", target = "supplyChainConsignmentItemGrossVolume")
    @Mapping(target = "id", ignore = true)
    ItemEntity map(Item value);

    @Mapping(target = "id", ignore = true)
    LogisticsShippingMarksCustomBarcodeEntity map(LogisticsShippingMarksCustomBarcode value);
}
