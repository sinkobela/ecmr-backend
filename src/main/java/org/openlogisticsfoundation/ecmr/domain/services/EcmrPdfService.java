/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.compositions.Item;
import org.openlogisticsfoundation.ecmr.domain.beans.ItemBean;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.PdfCreationException;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.PdfFile;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.renderers.Renderable;
import net.sf.jasperreports.renderers.SimpleDataRenderer;

@Service
@RequiredArgsConstructor
@Log4j2
public class EcmrPdfService {

    private final EcmrService ecmrService;
    private final ResourceLoader resourceLoader;
    private final EcmrPersistenceMapper ecmrPersistenceMapper;

    public PdfFile createJasperReportForEcmr(UUID id, InternalOrExternalUser internalOrExternalUser)
            throws NoPermissionException, EcmrNotFoundException, PdfCreationException {
        EcmrModel ecmrModel = this.ecmrService.getEcmr(id, internalOrExternalUser);
        return this.createJasperReportForEcmr(ecmrModel);
    }

    public PdfFile createJasperReportForEcmr(UUID id, String shareToken)
            throws NoPermissionException, EcmrNotFoundException, PdfCreationException {
        EcmrEntity ecmrEntity = this.ecmrService.getEcmrEntity(id);
        if (!ecmrEntity.getShareWithReaderToken().equals(shareToken)) {
            throw new NoPermissionException("Share Token mandatory");
        }
        return this.createJasperReportForEcmr(ecmrPersistenceMapper.toModel(ecmrEntity));
    }

    private PdfFile createJasperReportForEcmr(EcmrModel ecmrModel) throws PdfCreationException {
        try {
            InputStream ecmrReportStream = getClass().getResourceAsStream("/reports/ecmr.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(ecmrReportStream);

            List<ItemBean> itemBeans = convertToItemBeans(ecmrModel.getEcmrConsignment().getItemList());
            JRBeanCollectionDataSource itemDataSource = new JRBeanCollectionDataSource(itemBeans);
            HashMap<String, Object> parameters = setEcmrParameters(ecmrModel);
            parameters.put("items", itemDataSource);

            return new PdfFile("eCMR-" + ecmrModel.getEcmrConsignment().getReferenceIdentificationNumber() + ".pdf",
                    JasperRunManager.runReportToPdf(jasperReport, parameters, new JREmptyDataSource()));
        } catch (JRException e) {
            log.error(e);
            throw new PdfCreationException("Error generating report: " + e.getMessage());
        } catch (IOException e) {
            throw new PdfCreationException("I/O error occurred: " + e.getMessage());
        }
    }

    private HashMap<String, Object> setEcmrParameters(EcmrModel ecmrModel) throws IOException {
        HashMap<String, Object> parameters = new HashMap<>();

        //sender data
        parameters.put("senderNameCompany", ecmrModel.getEcmrConsignment().getSenderInformation().getSenderNameCompany());
        parameters.put("senderNamePerson", ecmrModel.getEcmrConsignment().getSenderInformation().getSenderNamePerson());
        parameters.put("senderStreet", ecmrModel.getEcmrConsignment().getSenderInformation().getSenderStreet());
        parameters.put("senderPostCode", ecmrModel.getEcmrConsignment().getSenderInformation().getSenderPostcode());
        parameters.put("senderCity", ecmrModel.getEcmrConsignment().getSenderInformation().getSenderCity());
        parameters.put("senderCountry", ecmrModel.getEcmrConsignment().getSenderInformation().getSenderCountryCode().getValue());

        //consignee Data
        parameters.put("consigneeNameCompany", ecmrModel.getEcmrConsignment().getConsigneeInformation().getConsigneeNameCompany());
        parameters.put("consigneeNamePerson", ecmrModel.getEcmrConsignment().getConsigneeInformation().getConsigneeNamePerson());
        parameters.put("consigneeStreet", ecmrModel.getEcmrConsignment().getConsigneeInformation().getConsigneeStreet());
        parameters.put("consigneePostcode", ecmrModel.getEcmrConsignment().getConsigneeInformation().getConsigneePostcode());
        parameters.put("consigneeCity", ecmrModel.getEcmrConsignment().getConsigneeInformation().getConsigneeCity());
        parameters.put("consigneeCountryCode", ecmrModel.getEcmrConsignment().getConsigneeInformation().getConsigneeCountryCode().getValue());

        //taking over The goods
        parameters.put("takingOverTheGoodsPlace", ecmrModel.getEcmrConsignment().getTakingOverTheGoods().getTakingOverTheGoodsPlace());
        if (ecmrModel.getEcmrConsignment().getTakingOverTheGoods().getLogisticsTimeOfArrivalDateTime() != null)
            parameters.put("logisticsTimeOfArrivalDateTime",
                    Date.from(ecmrModel.getEcmrConsignment().getTakingOverTheGoods().getLogisticsTimeOfArrivalDateTime()));
        if (ecmrModel.getEcmrConsignment().getTakingOverTheGoods().getLogisticsTimeOfDepartureDateTime() != null)
            parameters.put("logisticsTimeOfDepartureDateTime",
                    Date.from(ecmrModel.getEcmrConsignment().getTakingOverTheGoods().getLogisticsTimeOfDepartureDateTime()));

        //carrier Data
        parameters.put("carrierNameCompany", ecmrModel.getEcmrConsignment().getCarrierInformation().getCarrierNameCompany());
        parameters.put("carrierNamePerson", ecmrModel.getEcmrConsignment().getCarrierInformation().getCarrierNamePerson());
        parameters.put("carrierPostcode", ecmrModel.getEcmrConsignment().getCarrierInformation().getCarrierPostcode());
        parameters.put("carrierStreet", ecmrModel.getEcmrConsignment().getCarrierInformation().getCarrierStreet());
        parameters.put("carrierCity", ecmrModel.getEcmrConsignment().getCarrierInformation().getCarrierCity());
        parameters.put("carrierCountry", ecmrModel.getEcmrConsignment().getCarrierInformation().getCarrierCountryCode().getValue());
        parameters.put("carrierLicensePlate", ecmrModel.getEcmrConsignment().getCarrierInformation().getCarrierLicensePlate());

        //successive carrier Data
        parameters.put("successiveCarrierName", ecmrModel.getEcmrConsignment().getSuccessiveCarrierInformation().getSuccessiveCarrierNameCompany());
        parameters.put("successiveCarrierPersonName",
                ecmrModel.getEcmrConsignment().getSuccessiveCarrierInformation().getSuccessiveCarrierNamePerson());
        parameters.put("successiveCarrierStreetName", ecmrModel.getEcmrConsignment().getSuccessiveCarrierInformation().getSuccessiveCarrierStreet());
        parameters.put("successiveCarrierPostcode", ecmrModel.getEcmrConsignment().getSuccessiveCarrierInformation().getSuccessiveCarrierPostcode());
        parameters.put("successiveCarrierCity", ecmrModel.getEcmrConsignment().getSuccessiveCarrierInformation().getSuccessiveCarrierCity());
        parameters.put("successiveCarrierCountryCode",
                ecmrModel.getEcmrConsignment().getSuccessiveCarrierInformation().getSuccessiveCarrierCountryCode().getValue());

        //Carriers reservations
        parameters.put("carrierReservationsObservations",
                ecmrModel.getEcmrConsignment().getCarriersReservationsAndObservationsOnTakingOverTheGoods().getCarrierReservationsObservations());

        //Delivery of the goods
        parameters.put("deliveryOfTheGoodsPlace", ecmrModel.getEcmrConsignment().getDeliveryOfTheGoods().getLogisticsLocationCity());
        parameters.put("deliveryOfTheGoodsOpeningHours", ecmrModel.getEcmrConsignment().getDeliveryOfTheGoods().getLogisticsLocationOpeningHours());

        //Senders Instructions
        parameters.put("sendersInstructions", ecmrModel.getEcmrConsignment().getSendersInstructions().getTransportInstructionsDescription());

        //To be paid by
        parameters.put("customChargeCarriageValue", ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeCarriage().getValue());
        parameters.put("customChargeCarriageCurrency", ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeCarriage().getCurrency());
        if (ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeCarriage().getPayer() != null)
            parameters.put("customChargeCarriagePayer",
                    ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeCarriage().getPayer().toString());
        parameters.put("customChargeSupplementaryValue", ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeSupplementary().getValue());
        parameters.put("customChargeSupplementaryCurrency",
                ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeSupplementary().getCurrency());
        if (ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeSupplementary().getPayer() != null)
            parameters.put("customChargeSupplementaryPayer",
                    ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeSupplementary().getPayer().toString());
        parameters.put("customChargeCustomsDutiesValue", ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeCustomsDuties().getValue());
        parameters.put("customChargeCustomsDutiesCurrency",
                ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeCustomsDuties().getCurrency());
        if (ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeCustomsDuties().getPayer() != null)
            parameters.put("customChargeCustomsDutiesPayer",
                    ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeCustomsDuties().getPayer().toString());
        parameters.put("customChargeOtherValue", ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeOther().getValue());
        parameters.put("customChargeOtherCurrency", ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeOther().getCurrency());
        if (ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeOther().getPayer() != null)
            parameters.put("customChargeOtherPayer", ecmrModel.getEcmrConsignment().getToBePaidBy().getCustomChargeOther().getPayer().toString());

        //Documents
        parameters.put("documentsRemarks", ecmrModel.getEcmrConsignment().getDocumentsHandedToCarrier().getDocumentsRemarks());

        //Special Agreements
        parameters.put("customSpecialAgreement", ecmrModel.getEcmrConsignment().getSpecialAgreementsSenderCarrier().getCustomSpecialAgreement());

        //Particulars
        parameters.put("customParticulars", ecmrModel.getEcmrConsignment().getOtherUsefulParticulars().getCustomParticulars());

        //Cash on delivery
        parameters.put("customCashOnDelivery", ecmrModel.getEcmrConsignment().getCashOnDelivery().getCustomCashOnDelivery());

        //Established
        if (ecmrModel.getEcmrConsignment().getEstablished().getCustomEstablishedDate() != null)
            parameters.put("customEstablishedDate", Date.from(ecmrModel.getEcmrConsignment().getEstablished().getCustomEstablishedDate()));
        parameters.put("customEstablishedIn", ecmrModel.getEcmrConsignment().getEstablished().getCustomEstablishedIn());

        //Sender Signature
        if (ecmrModel.getEcmrConsignment().getSignatureOrStampOfTheSender().getSenderSignature() != null) {
            Renderable renderableSignature =
                    this.decodeImage(ecmrModel.getEcmrConsignment().getSignatureOrStampOfTheSender().getSenderSignature().getData());
            parameters.put("senderSignature", renderableSignature);
        }

        //Carrier Signature
        if (ecmrModel.getEcmrConsignment().getSignatureOrStampOfTheCarrier().getCarrierSignature() != null) {
            Renderable renderableSignature =
                    this.decodeImage(ecmrModel.getEcmrConsignment().getSignatureOrStampOfTheCarrier().getCarrierSignature().getData());
            parameters.put("carrierSignature", renderableSignature);
            parameters.put("consigneeSigningLocation",
                    ecmrModel.getEcmrConsignment().getSignatureOrStampOfTheSender().getSenderSignature().getUserCity());
            parameters.put("consigneeTimeOfArrival",
                    Date.from(ecmrModel.getEcmrConsignment().getSignatureOrStampOfTheSender().getSenderSignature().getTimestamp()));
        }

        //Consignee Signature
        if (ecmrModel.getEcmrConsignment().getGoodsReceived().getConsigneeSignature() != null) {
            Renderable renderableSignature =
                    this.decodeImage(ecmrModel.getEcmrConsignment().getGoodsReceived().getConsigneeSignature().getData());
            parameters.put("consigneeSignature", renderableSignature);
        }

        //National International Information Text
        boolean isNational = isNationalTransport(ecmrModel);
        parameters.put("DE_InternationalNationalTransport", getInformationText("DE", isNational));
        parameters.put("EN_InternationalNationalTransport", getInformationText("EN", isNational));

        parameters.put("nonContractualCarrierRemarks",
                ecmrModel.getEcmrConsignment().getNonContractualPartReservedForTheCarrier().getNonContractualCarrierRemarks());
        parameters.put("referenceId", ecmrModel.getEcmrConsignment().getReferenceIdentificationNumber().getValue());
        parameters.put("ecmrId", ecmrModel.getEcmrId());

        //eCmr Logo
        InputStream imageStream = resourceLoader.getResource("classpath:/images/cmrLogo.png").getInputStream();
        byte[] waterMarkBytes = imageStream.readAllBytes();
        Renderable renderableWaterMark = SimpleDataRenderer.getInstance(waterMarkBytes);
        parameters.put("ecmrLogo", renderableWaterMark);

        return parameters;
    }

    private String getInformationText(String language, boolean isNational) throws IOException {
        String filePath = "reports/texts/" + language + (isNational ? "_NationalTransport.txt" : "_InternationalTransport.txt");
        InputStream resource = new ClassPathResource(filePath).getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
        return reader.lines().collect(Collectors.joining());
    }

    private boolean isNationalTransport(EcmrModel ecmrModel) {
        String senderCountry = ecmrModel.getEcmrConsignment().getSenderInformation().getSenderCountryCode().getValue();
        String consigneeCountry = ecmrModel.getEcmrConsignment().getConsigneeInformation().getConsigneeCountryCode().getValue();

        if (senderCountry == null || consigneeCountry == null) {
            return false;
        }

        return senderCountry.equals(consigneeCountry);
    }

    private Renderable decodeImage(String base64Image) throws IOException {
        try {
            if (base64Image == null || !base64Image.contains(",")) {
                throw new IllegalArgumentException("Invalid base64 image string");
            }
            String base64ImageString = base64Image.split(",")[1];
            byte[] imageBytes = Base64.getDecoder().decode(base64ImageString);
            return SimpleDataRenderer.getInstance(imageBytes);
        } catch (Exception e) {
            log.error("Error while decoding image", e);
            throw new IOException(e);
        }
    }

    private List<ItemBean> convertToItemBeans(List<Item> items) {
        List<ItemBean> itemBeans = new ArrayList<>();

        for (Item item : items) {
            ItemBean itemBean = new ItemBean();
            itemBean.setLogisticsShippingMarksMarking(item.getMarksAndNos().getLogisticsShippingMarksMarking());
            itemBean.setLogisticsShippingMarksCustomBarcode(item.getMarksAndNos().getLogisticsShippingMarksCustomBarcode());
            itemBean.setLogisticsPackageItemQuantity(item.getNumberOfPackages().getLogisticsPackageItemQuantity());
            itemBean.setLogisticsPackageType(item.getMethodOfPacking().getLogisticsPackageType());
            itemBean.setTransportCargoIdentification(item.getNatureOfTheGoods().getTransportCargoIdentification());
            itemBean.setSupplyChainConsignmentItemGrossWeight(item.getGrossWeightInKg().getSupplyChainConsignmentItemGrossWeight());
            itemBean.setSupplyChainConsignmentItemGrossVolume(item.getVolumeInM3().getSupplyChainConsignmentItemGrossVolume());
            itemBeans.add(itemBean);
        }
        return itemBeans;
    }
}
