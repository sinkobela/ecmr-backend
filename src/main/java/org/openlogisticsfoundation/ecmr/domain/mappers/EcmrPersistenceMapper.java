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
import org.mapstruct.Named;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.api.model.SealedDocument;
import org.openlogisticsfoundation.ecmr.api.model.SealedEcmr;
import org.openlogisticsfoundation.ecmr.api.model.areas.one.SenderInformation;
import org.openlogisticsfoundation.ecmr.api.model.areas.seven.SuccessiveCarrierInformation;
import org.openlogisticsfoundation.ecmr.api.model.areas.six.CarrierInformation;
import org.openlogisticsfoundation.ecmr.api.model.areas.twentyfour.GoodsReceived;
import org.openlogisticsfoundation.ecmr.api.model.areas.two.ConsigneeInformation;
import org.openlogisticsfoundation.ecmr.api.model.compositions.Item;
import org.openlogisticsfoundation.ecmr.api.model.signature.Signature;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.commands.CustomChargeCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ItemCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.*;

@Mapper(componentModel = "spring")
public interface EcmrPersistenceMapper {

    @Mapping(target = "senderInformation.id", ignore = true)
    @Mapping(target = "carrierInformation.id", ignore = true)
    @Mapping(target = "consigneeInformation.id", ignore = true)
    @Mapping(target = "successiveCarrierInformation.id", ignore = true)
    @Mapping(target = "itemList", qualifiedByName = "mapItem")
    @Mapping(target = "deliveryOfTheGoods.id", ignore = true)
    @Mapping(target = "takingOverTheGoods.id", ignore = true)
    @Mapping(target = "goodsReceived.id", ignore = true)
    @Mapping(target = "toBePaidBy.id", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ecmrId", ignore = true)
    @Mapping(target = "senderInformation.signature", ignore = true)
    @Mapping(target = "carrierInformation.signature", ignore = true)
    @Mapping(target = "consigneeInformation.signature", ignore = true)
    @Mapping(target = "successiveCarrierInformation.signature", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "editedBy", ignore = true)
    @Mapping(target = "editedAt", ignore = true)
    @Mapping(target = "shareWithSenderToken", ignore = true)
    @Mapping(target = "shareWithCarrierToken", ignore = true)
    @Mapping(target = "shareWithConsigneeToken", ignore = true)
    EcmrEntity toEntity(EcmrCommand ecmrModel, EcmrType type, EcmrStatus status);

    @Named("mapItem")
    @Mapping(target = "id", ignore = true)
    ItemEntity map(ItemCommand value);

    @Mapping(target = "id", ignore = true)
    CustomChargeEntity map(CustomChargeCommand value);

    @Mapping(target = "ecmrId", source = "ecmrId")
    @Mapping(target = "ecmrStatus", source = "ecmrStatus")
    @Mapping(target = "ecmrConsignment.senderInformation", source = "senderInformation")
    @Mapping(target = "ecmrConsignment.consigneeInformation", source = "consigneeInformation")
    @Mapping(target = "ecmrConsignment.deliveryOfTheGoods", source = "deliveryOfTheGoods")
    @Mapping(target = "ecmrConsignment.sendersInstructions.transportInstructionsDescription", source = "transportInstructionsDescription")
    @Mapping(target = "ecmrConsignment.carrierInformation", source = "carrierInformation")
    @Mapping(target = "ecmrConsignment.successiveCarrierInformation", source = "successiveCarrierInformation")
    @Mapping(target = "ecmrConsignment.carriersReservationsAndObservationsOnTakingOverTheGoods.senderReservationsObservationsSignature", source = "senderInformation.signature")
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
    @Mapping(target = "ecmrConsignment.signatureOrStampOfTheSender.senderSignature", source = "senderInformation.signature")
    @Mapping(target = "ecmrConsignment.signatureOrStampOfTheCarrier.carrierSignature", source = "carrierInformation.signature")
    @Mapping(target = "ecmrConsignment.goodsReceived", source = "value", qualifiedByName = "goodsReceived")
    @Mapping(target = "ecmrConsignment.nonContractualPartReservedForTheCarrier.nonContractualCarrierRemarks", source = "nonContractualCarrierRemarks")
    @Mapping(target = "ecmrConsignment.referenceIdentificationNumber.value", source = "referenceIdentificationNumber")
    EcmrModel toModel(EcmrEntity value);

    @Named("goodsReceived")
    @Mapping(target = "confirmedLogisticsLocationName", source = "goodsReceived.confirmedLogisticsLocationName")
    @Mapping(target = "consigneeReservationsObservations", source = "goodsReceived.consigneeReservationsObservations")
    @Mapping(target = "consigneeTimeOfArrival", source = "goodsReceived.consigneeTimeOfArrival")
    @Mapping(target = "consigneeTimeOfDeparture", source = "goodsReceived.consigneeTimeOfDeparture")
    @Mapping(target = "consigneeSignature", source = "consigneeInformation.signature")
    @Mapping(target = "consigneeSignatureDate", source = "consigneeInformation.signature.timestamp")
    GoodsReceived toGoodsReceived(EcmrEntity ecmrEntity);

    @Mapping(source = "street", target = "senderStreet")
    @Mapping(source = "namePerson", target = "senderNamePerson")
    @Mapping(source = "nameCompany", target = "senderNameCompany")
    @Mapping(source = "postcode", target = "senderPostcode")
    @Mapping(source = "countryCode", target = "senderCountryCode.value")
    @Mapping(source = "city", target = "senderCity")
    @Mapping(source = "email", target = "senderContactInformation.email")
    @Mapping(source = "phone", target = "senderContactInformation.phone")
    SenderInformation map(SenderInformationEntity value);

    @Mapping(source = "street", target = "consigneeStreet")
    @Mapping(source = "namePerson", target = "consigneeNamePerson")
    @Mapping(source = "nameCompany", target = "consigneeNameCompany")
    @Mapping(source = "postcode", target = "consigneePostcode")
    @Mapping(source = "countryCode", target = "consigneeCountryCode.value")
    @Mapping(source = "city", target = "consigneeCity")
    @Mapping(source = "email", target = "consigneeContactInformation.email")
    @Mapping(source = "phone", target = "consigneeContactInformation.phone")
    ConsigneeInformation map(ConsigneeInformationEntity value);

    @Mapping(source = "street", target = "carrierStreet")
    @Mapping(source = "namePerson", target = "carrierNamePerson")
    @Mapping(source = "nameCompany", target = "carrierNameCompany")
    @Mapping(source = "postcode", target = "carrierPostcode")
    @Mapping(source = "countryCode", target = "carrierCountryCode.value")
    @Mapping(source = "city", target = "carrierCity")
    @Mapping(source = "email", target = "carrierContactInformation.email")
    @Mapping(source = "phone", target = "carrierContactInformation.phone")
    @Mapping(source = "carrierLicensePlate", target = "carrierLicensePlate")
    CarrierInformation map(CarrierInformationEntity value);

    @Mapping(source = "street", target = "successiveCarrierStreet")
    @Mapping(source = "namePerson", target = "successiveCarrierNamePerson")
    @Mapping(source = "nameCompany", target = "successiveCarrierNameCompany")
    @Mapping(source = "postcode", target = "successiveCarrierPostcode")
    @Mapping(source = "countryCode", target = "successiveCarrierCountryCode.value")
    @Mapping(source = "city", target = "successiveCarrierCity")
    @Mapping(source = "signature", target = "successiveCarrierSignature")
    @Mapping(source = "signature.timestamp", target = "successiveCarrierSignatureDate")
    @Mapping(source = "email", target = "successiveCarrierContactInformation.email")
    @Mapping(source = "phone", target = "successiveCarrierContactInformation.phone")
    SuccessiveCarrierInformation map(SuccessiveCarrierInformationEntity value);

    @Mapping(source = "logisticsPackageItemQuantity", target = "numberOfPackages.logisticsPackageItemQuantity")
    @Mapping(source = "logisticsPackageType", target = "methodOfPacking.logisticsPackageType")
    @Mapping(source = "logisticsShippingMarksMarking", target = "marksAndNos.logisticsShippingMarksMarking")
    @Mapping(source = "logisticsShippingMarksCustomBarcode", target = "marksAndNos.logisticsShippingMarksCustomBarcode")
    @Mapping(source = "supplyChainConsignmentItemGrossVolume", target = "volumeInM3.supplyChainConsignmentItemGrossVolume")
    @Mapping(source = "supplyChainConsignmentItemGrossWeight", target = "grossWeightInKg.supplyChainConsignmentItemGrossWeight")
    @Mapping(source = "transportCargoIdentification", target = "natureOfTheGoods.transportCargoIdentification")
    Item map(ItemEntity value);

    @Mapping(target = "shareWithSenderToken", ignore = true)
    @Mapping(target = "shareWithCarrierToken", ignore = true)
    @Mapping(target = "shareWithConsigneeToken", ignore = true)
    EcmrEntity toEntity(@MappingTarget EcmrEntity ecmrEntity, EcmrCommand ecmrCommand, EcmrType ecmrType);

    @Mapping(target = "type", source = "signatureType", ignore = false)
    Signature signatureEntityToSignature(SignatureEntity signatureEntity);

    SealedEcmrEntity toEntity(SealedEcmr sealedEcmr);

    EcmrSealingMetadataEntity toEntity(SealedEcmr.Metadata sealedEcmrMetadata);

    SealedDocument toModel(SealedDocumentEntity sealedDocumentEntity);

    SealedDocumentEntity toEntity(SealedDocument sealedDocument);
}
