/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.api.model.SealedDocument;
import org.openlogisticsfoundation.ecmr.api.model.TransportRole;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.SealAlreadyPresentException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.SealedDocumentNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.SealedDocumentNotValidException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.mappers.SealedDocumentPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.SealedDocumentWithoutEcmr;
import org.openlogisticsfoundation.ecmr.domain.models.commands.SealCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.CarrierInformationEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrMemberEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrSealEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.GoodsReceivedEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.ItemEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.SealedDocumentEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.TakingOverTheGoodsEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.SealedDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ecmr.seal.sign.dss.SignerService;
import ecmr.seal.verify.dss.VerifierService;
import ecmr.seal.verify.rest.ESeal;
import ecmr.seal.verify.rest.SealVerifyResult;
import ecmr.seal.verify.rest.VerifyRequest;
import ecmr.seal.verify.rest.VerifyResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SealedDocumentService {

    private final SignerService signerService;
    private final VerifierService verifierService;
    private final SealedDocumentRepository sealedDocumentRepository;
    private final SealedDocumentPersistenceMapper sealedDocumentPersistenceMapper;
    private final EcmrService ecmrService;
    private final AuthorisationService authorisationService;
    private final EcmrStatusService ecmrStatusService;
    private final EcmrPersistenceMapper ecmrPersistenceMapper;

    @Transactional
    public void sealEcmr(UUID ecmrId, SealCommand sealCommand, InternalOrExternalUser internalOrExternalUser)
            throws EcmrNotFoundException, SealAlreadyPresentException, ValidationException, NoPermissionException {

        Optional<SealedDocumentEntity> sealedDocumentOpt = this.getSealedDocumentEntity(ecmrId, internalOrExternalUser);
        SealedDocumentEntity sealedDocument;
        if (sealedDocumentOpt.isPresent()) {
            sealedDocument = sealedDocumentOpt.get();
            this.validateSealedDocument(sealedDocument);
        } else {
            EcmrEntity ecmr = ecmrService.getEcmrEntity(ecmrId);
            sealedDocument = new SealedDocumentEntity();
            sealedDocument.setEcmr(ecmr);
        }
        this.validateEcmrForSealing(sealCommand.getTransportRole(), sealedDocument, internalOrExternalUser, ecmrId);
        SealedDocumentEntity sealedDocumentEntity = this.createSeal(sealedDocument, sealCommand, internalOrExternalUser.getFullName());

        this.ecmrStatusService.setEcmrStatus(sealedDocumentEntity, internalOrExternalUser);
    }

    public SealVerifyResult verify(List<ESeal> seals) {
        VerifyRequest request = new VerifyRequest(seals, null, false, false, false, false);
        VerifyResponse result = verifierService.verify(request);
        return result.getResult();
    }

    public SealedDocumentWithoutEcmr getSealedDocumentWithoutEcmr(UUID ecmrId, InternalOrExternalUser internalOrExternalUser)
            throws SealedDocumentNotFoundException, NoPermissionException {
        if (authorisationService.hasNoRole(internalOrExternalUser, ecmrId)) {
            throw new NoPermissionException("No permission to load ecmr");
        }
        return this.sealedDocumentRepository.findProjectionByEcmrId(ecmrId).map(sealedDocumentPersistenceMapper::toDomainWithoutEcmr)
                .orElseThrow(() -> new SealedDocumentNotFoundException(ecmrId));
    }

    public boolean sealExists(UUID ecmrId) {
        return sealedDocumentRepository.existsByEcmr_EcmrId(ecmrId);
    }

    public Optional<SealedDocumentEntity> getCurrentSealedDocument(UUID ecmrId) {
        return sealedDocumentRepository.findByEcmrId(ecmrId);
    }

    public String getCurrentSeal(SealedDocumentEntity sealedEcmr) {
        if (sealedEcmr.getConsigneeSeal() != null) {
            return sealedEcmr.getConsigneeSeal().getSeal();
        } else if (sealedEcmr.getCarrierSeal() != null) {
            return sealedEcmr.getCarrierSeal().getSeal();
        } else if (sealedEcmr.getSenderSeal() != null) {
            return sealedEcmr.getSenderSeal().getSeal();
        } else {
            return null;
        }
    }

    public void validateSealedDocument(SealedDocumentEntity sealedDocument) throws ValidationException {
        if (sealedDocument.getSenderSeal() == null) {
            throw new ValidationException("Sender seal is missing");
        }
        if (sealedDocument.getConsigneeSeal() != null && sealedDocument.getCarrierSeal() == null) {
            throw new ValidationException("Carrier seal is missing");
        }
    }

    Optional<SealedDocument> getSealedDocument(UUID ecmrId, InternalOrExternalUser internalOrExternalUser) throws NoPermissionException {
        if (authorisationService.hasNoRole(internalOrExternalUser, ecmrId)) {
            throw new NoPermissionException("No permission to load ecmr");
        }
        return sealedDocumentRepository.findByEcmrId(ecmrId).map(sealedDocumentPersistenceMapper::toDomain);
    }

    Optional<SealedDocumentEntity> getSealedDocumentEntity(UUID ecmrId, InternalOrExternalUser internalOrExternalUser) throws NoPermissionException {
        if (authorisationService.hasNoRole(internalOrExternalUser, ecmrId)) {
            throw new NoPermissionException("No permission to load ecmr");
        }
        return sealedDocumentRepository.findByEcmrId(ecmrId);
    }

    Optional<SealedDocumentEntity> getSealedDocumentEntity(UUID ecmrId) {
        return sealedDocumentRepository.findByEcmrId(ecmrId);
    }

    private SealedDocumentEntity createSeal(SealedDocumentEntity sealedDocumentEntity, SealCommand sealCommand, String sealer) {
        // 1. seal ecmr
        Map<String, Object> claims = new HashMap<>();
        claims.put("ecmr", ecmrPersistenceMapper.toModel(sealedDocumentEntity.getEcmr()));
        String seal = signerService.sign(claims, this.getCurrentSeal(sealedDocumentEntity));
        EcmrSealEntity sealEntity = this.createSealEntity(seal, sealCommand, sealer);

        switch (sealCommand.getTransportRole()) {
        case SENDER -> sealedDocumentEntity.setSenderSeal(sealEntity);
        case CARRIER -> sealedDocumentEntity.setCarrierSeal(sealEntity);
        case CONSIGNEE -> sealedDocumentEntity.setConsigneeSeal(sealEntity);
        }

        return sealedDocumentRepository.save(sealedDocumentEntity);
    }

    private void validateEcmrForSealing(TransportRole transportRole, SealedDocumentEntity sealedEcmr, InternalOrExternalUser internalOrExternalUser,
            UUID ecmrId) throws ValidationException, NoPermissionException, SealAlreadyPresentException {
        if (transportRole == TransportRole.SENDER) {
            if (authorisationService.doesNotHaveRole(internalOrExternalUser, ecmrId, EcmrRole.Sender)) {
                throw new NoPermissionException("Sender sealing but no Sender Role");
            }
            if (sealedEcmr.getSenderSeal() != null) {
                throw new SealAlreadyPresentException(ecmrId, transportRole);
            }
            this.validateEcmrStatus(EcmrStatus.NEW, sealedEcmr.getEcmr());
            this.validateFieldsSender(sealedEcmr.getEcmr());
        } else if (transportRole == TransportRole.CARRIER) {
            if (authorisationService.doesNotHaveRole(internalOrExternalUser, ecmrId, EcmrRole.Carrier)) {
                throw new NoPermissionException("Carrier sealing but no Carrier Role");
            }
            if (sealedEcmr.getCarrierSeal() != null) {
                throw new SealAlreadyPresentException(ecmrId, transportRole);
            } else if (sealedEcmr.getSenderSeal() == null) {
                throw new SealedDocumentNotValidException("Sender seal missing");
            }
            this.validateEcmrStatus(EcmrStatus.LOADING, sealedEcmr.getEcmr());
        } else if (transportRole == TransportRole.CONSIGNEE) {
            if (authorisationService.doesNotHaveRole(internalOrExternalUser, ecmrId, EcmrRole.Consignee)) {
                throw new NoPermissionException("Consignee sealing but no Consignee Role");
            }
            if (sealedEcmr.getConsigneeSeal() != null) {
                throw new SealAlreadyPresentException(ecmrId, transportRole);
            } else if (sealedEcmr.getSenderSeal() == null || sealedEcmr.getCarrierSeal() == null) {
                throw new SealedDocumentNotValidException("Sender or Carrier seal missing");
            }
            this.validateEcmrStatus(EcmrStatus.IN_TRANSPORT, sealedEcmr.getEcmr());
            this.validateFieldsConsignee(sealedEcmr.getEcmr());
        } else {
            throw new ValidationException("Transport Role " + transportRole + " not implemented");
        }
    }

    private void validateEcmrStatus(EcmrStatus requiredStatus, EcmrEntity ecmr) throws ValidationException {
        if (ecmr.getEcmrStatus() != requiredStatus) {
            throw new ValidationException("Ecmr status needs to be " + requiredStatus.name());
        }
    }

    private void validateFieldsSender(EcmrEntity ecmr) throws ValidationException {
        this.checkEcmrMemberInformation(ecmr.getSenderInformation(), "SenderInformation");
        if (!ecmr.getIsMultiConsigneeShipment()) {
            this.checkEcmrMemberInformation(ecmr.getConsigneeInformation(), "ConsigneeInformation");
        }
        TakingOverTheGoodsEntity takingOverTheGoods = ecmr.getTakingOverTheGoods();
        if (takingOverTheGoods == null) {
            throw new ValidationException("Taking Over The Goods information is missing");
        }
        if (StringUtils.isBlank(takingOverTheGoods.getTakingOverTheGoodsPlace())
                || takingOverTheGoods.getLogisticsTimeOfArrivalDateTime() == null
                || takingOverTheGoods.getLogisticsTimeOfDepartureDateTime() == null) {
            throw new ValidationException("Taking Over The Goods information is missing");
        }
        CarrierInformationEntity carrierInformation = ecmr.getCarrierInformation();
        this.checkEcmrMemberInformation(carrierInformation, "CarrierInformation");
        if (StringUtils.isBlank(carrierInformation.getCarrierLicensePlate())) {
            throw new ValidationException("Carrier License Plate is missing");
        }
        if (StringUtils.isBlank(ecmr.getCustomEstablishedIn())
                || ecmr.getCustomEstablishedDate() == null) {
            throw new ValidationException("Custom Established Information is missing");
        }

        for (ItemEntity item : ecmr.getItemList()) {
            this.checkItem(item);
        }
    }

    private void validateFieldsConsignee(EcmrEntity ecmr) throws ValidationException {
        GoodsReceivedEntity goodsReceived = ecmr.getGoodsReceived();
        if (goodsReceived == null || StringUtils.isBlank(goodsReceived.getConfirmedLogisticsLocationName())) {
            throw new ValidationException("Goods Received is missing");
        }
    }

    private void checkEcmrMemberInformation(EcmrMemberEntity memberEntity, String name) throws ValidationException {
        if (memberEntity == null) {
            throw new ValidationException(name + " is missing");
        }
        if (StringUtils.isBlank(memberEntity.getCompanyName())
                || StringUtils.isBlank(memberEntity.getStreet())
                || StringUtils.isBlank(memberEntity.getPostcode())
                || StringUtils.isBlank(memberEntity.getCity())
                || StringUtils.isBlank(memberEntity.getCountryCode())
        ) {
            throw new ValidationException("Field in " + name + " is missing");
        }
    }

    private void checkItem(ItemEntity item) throws ValidationException {
        if (item == null) {
            throw new ValidationException("Item is missing");
        }
        if ( StringUtils.isBlank(item.getLogisticsShippingMarksMarking())
                || item.getLogisticsShippingMarksCustomBarcodeList().isEmpty()
                || StringUtils.isBlank(item.getLogisticsShippingMarksCustomBarcodeList().getFirst().getBarcode())
                || item.getLogisticsPackageItemQuantity() == null
                || StringUtils.isBlank(item.getLogisticsPackageType())
                || StringUtils.isBlank(item.getTransportCargoIdentification())
                || item.getSupplyChainConsignmentItemGrossWeight() == null
                || item.getSupplyChainConsignmentItemGrossVolume() == 0) {
            throw new ValidationException("Field in Item is missing");
        }
    }

    private EcmrSealEntity createSealEntity(String seal, SealCommand sealCommand, String sealer) {
        EcmrSealEntity ecmrSealEntity = new EcmrSealEntity();
        ecmrSealEntity.setSeal(seal);
        ecmrSealEntity.setSealer(sealer);
        ecmrSealEntity.setTimestamp(Instant.now());
        ecmrSealEntity.setTransportRole(sealCommand.getTransportRole());
        return ecmrSealEntity;
    }
}
