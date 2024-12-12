/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.time.Instant;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.api.model.signature.Signature;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.SignatureAlreadyPresentException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.SignatureType;
import org.openlogisticsfoundation.ecmr.domain.models.Signer;
import org.openlogisticsfoundation.ecmr.domain.models.commands.SealCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.SignCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.*;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EcmrSignService {
    private final EcmrRepository ecmrRepository;
    private final EcmrPersistenceMapper ecmrPersistenceMapper;
    private final EcmrService ecmrService;
    private final AuthorisationService authorisationService;
    private final ESealService eSealService;

    @Value("${app.origin.url}")
    private String originUrl;

    // Todo: check whether the role is allowed to use the specific signature type
    @Transactional
    public Signature signEcmr(InternalOrExternalUser internalOrExternalUser, UUID ecmrId, SignCommand signCommand, SignatureType signatureType)
            throws EcmrNotFoundException, SignatureAlreadyPresentException, ValidationException, NoPermissionException {
        EcmrEntity ecmrEntity = ecmrRepository.findByEcmrId(ecmrId).orElseThrow(() -> new EcmrNotFoundException(ecmrId));

        SignatureEntity signatureEntity = createSignatureEntity(signCommand.getSigner(), ecmrEntity, internalOrExternalUser, signatureType, signCommand.getCity());
        signatureEntity.setData(signCommand.getData());

        this.ecmrService.setEcmrStatus(ecmrEntity);
        return ecmrPersistenceMapper.signatureEntityToSignature(signatureEntity);
    }

    // Todo: check whether the role is allowed to use the specific signature type
    @Transactional
    public Signature sealEcmr(InternalOrExternalUser internalOrExternalUser, UUID ecmrId, SealCommand sealCommand, SignatureType signatureType)
        throws EcmrNotFoundException, SignatureAlreadyPresentException, ValidationException, NoPermissionException {
        EcmrEntity ecmrEntity = ecmrRepository.findByEcmrId(ecmrId).orElseThrow(() -> new EcmrNotFoundException(ecmrId));
        ecmrEntity.setOriginUrl(originUrl);

        SignatureEntity signatureEntity = createSignatureEntity(sealCommand.getSigner(), ecmrEntity, internalOrExternalUser, signatureType, sealCommand.getCity());

        SealedEcmrEntity sealedEcmr = new SealedEcmrEntity();
        sealedEcmr.setEcmr(ecmrEntity);
        EcmrSealingMetadataEntity metadata = new EcmrSealingMetadataEntity();
        metadata.setSealer(sealCommand.getSigner().name());
        metadata.setTimestamp(Instant.now());
        sealedEcmr.setMetadata(metadata);

        String seal = eSealService.sealEcmr(sealedEcmr, sealCommand.getPrecedingSeal());
        signatureEntity.setData(seal);

        this.ecmrService.setEcmrStatus(ecmrEntity);
        return ecmrPersistenceMapper.signatureEntityToSignature(signatureEntity);
    }

    private SignatureEntity createSignatureEntity(Signer signer, EcmrEntity ecmrEntity, InternalOrExternalUser internalOrExternalUser, SignatureType signatureType, String city)
        throws ValidationException, NoPermissionException, SignatureAlreadyPresentException {
        UUID ecmrId = ecmrEntity.getEcmrId();
        SignatureEntity signatureEntity = new SignatureEntity();

        signatureEntity.setTimestamp(Instant.now());
        signatureEntity.setUserName(internalOrExternalUser.getFullName());
        signatureEntity.setUserCountry((internalOrExternalUser.isInternalUser()) ? internalOrExternalUser.getInternalUser().getCountry().name() :
            null);
        signatureEntity.setSignatureType(signatureType);
        signatureEntity.setUserCity(city);

        switch (signer) {
            case Signer.Sender -> {
                if(authorisationService.doesNotHaveRole(internalOrExternalUser, ecmrId, EcmrRole.Sender)) {
                    throw new NoPermissionException("Sender sign but no Sender Role");
                }
                if (ecmrEntity.getSenderInformation().getSignature() != null) {
                    throw new SignatureAlreadyPresentException(ecmrId, signer.name());
                }
                this.validateEcmrStatus(EcmrStatus.NEW, ecmrEntity);
                this.validateFieldsSender(ecmrEntity);
                ecmrEntity.getSenderInformation().setSignature(signatureEntity);
            }
            case Signer.Carrier -> {
                if(authorisationService.doesNotHaveRole(internalOrExternalUser, ecmrId, EcmrRole.Carrier)) {
                    throw new NoPermissionException("Carrier sign but no Carrier Role");
                }
                if (ecmrEntity.getCarrierInformation().getSignature() != null) {
                    throw new SignatureAlreadyPresentException(ecmrId, signer.name());
                }
                this.validateEcmrStatus(EcmrStatus.LOADING, ecmrEntity);
                ecmrEntity.getCarrierInformation().setSignature(signatureEntity);
            }
            case Signer.Consignee -> {
                if(authorisationService.doesNotHaveRole(internalOrExternalUser, ecmrId, EcmrRole.Consignee)) {
                    throw new NoPermissionException("Consignee sign but no Consignee Role");
                }
                if (ecmrEntity.getConsigneeInformation().getSignature() != null) {
                    throw new SignatureAlreadyPresentException(ecmrId, signer.name());
                }
                this.validateEcmrStatus(EcmrStatus.IN_TRANSPORT, ecmrEntity);
                this.validateFieldsConsignee(ecmrEntity);
                ecmrEntity.getConsigneeInformation().setSignature(signatureEntity);
            }
            default -> throw new ValidationException("Signature Type " + signer.name() + " not valid");
        }

        return signatureEntity;
    }

    private void validateEcmrStatus(EcmrStatus requieredStatus, EcmrEntity ecmrEntity) throws ValidationException {
        if(ecmrEntity.getEcmrStatus() != requieredStatus) {
            throw new ValidationException("Ecmr status needs to be "+requieredStatus.name());
        }
    }

    private void validateFieldsSender(EcmrEntity ecmrEntity) throws ValidationException {
        this.checkEcmrMemberEntity(ecmrEntity.getSenderInformation(), "Sender Information");
        this.checkEcmrMemberEntity(ecmrEntity.getConsigneeInformation(), "Consignee Information");
        TakingOverTheGoodsEntity takingOverTheGoods = ecmrEntity.getTakingOverTheGoods();
        if (takingOverTheGoods == null) {
            throw new ValidationException("Taking Over The Goods information is missing");
        }
        if (StringUtils.isBlank(takingOverTheGoods.getTakingOverTheGoodsPlace())
                || takingOverTheGoods.getLogisticsTimeOfArrivalDateTime() == null
                || takingOverTheGoods.getLogisticsTimeOfDepartureDateTime() == null) {
            throw new ValidationException("Taking Over The Goods information is missing");
        }
        CarrierInformationEntity carrierInformation = ecmrEntity.getCarrierInformation();
        this.checkEcmrMemberEntity(carrierInformation, "Carrier Information");
        if (StringUtils.isBlank(carrierInformation.getCarrierLicensePlate())) {
            throw new ValidationException("Carrier License Plate is missing");
        }
        if (StringUtils.isBlank(ecmrEntity.getCustomEstablishedIn())
                || ecmrEntity.getCustomEstablishedDate() == null) {
            throw new ValidationException("Custom Established Information is missing");
        }

        for (ItemEntity itemEntity : ecmrEntity.getItemList()) {
            this.checkItemEntity(itemEntity);
        }
    }

    private void validateFieldsConsignee(EcmrEntity ecmrEntity) throws ValidationException {
        GoodsReceivedEntity goodsReceived = ecmrEntity.getGoodsReceived();
        if (goodsReceived == null || StringUtils.isBlank(goodsReceived.getConfirmedLogisticsLocationName())) {
            throw new ValidationException("Goods Received is missing");
        }
    }

    private void checkEcmrMemberEntity(EcmrMemberEntity memberEntity, String fieldName) throws ValidationException {
        if (memberEntity == null) {
            throw new ValidationException(fieldName + " is missing");
        }
        if (StringUtils.isBlank(memberEntity.getNameCompany())
                || StringUtils.isBlank(memberEntity.getStreet())
                || StringUtils.isBlank(memberEntity.getPostcode())
                || StringUtils.isBlank(memberEntity.getCity())
                || StringUtils.isBlank(memberEntity.getCountryCode())
        ) {
            throw new ValidationException("Field in " + fieldName + " is missing");
        }
    }

    private void checkItemEntity(ItemEntity itemEntity) throws ValidationException {
        if (itemEntity == null) {
            throw new ValidationException("Item is missing");
        }
        if (StringUtils.isBlank(itemEntity.getLogisticsShippingMarksMarking())
                || StringUtils.isBlank(itemEntity.getLogisticsShippingMarksCustomBarcode())
                || itemEntity.getLogisticsPackageItemQuantity() == null
                || StringUtils.isBlank(itemEntity.getLogisticsPackageType())
                || StringUtils.isBlank(itemEntity.getTransportCargoIdentification())
                || itemEntity.getSupplyChainConsignmentItemGrossWeight() == null
                || itemEntity.getSupplyChainConsignmentItemGrossVolume() == 0) {
            throw new ValidationException("Field in Item is missing");
        }
    }

}
