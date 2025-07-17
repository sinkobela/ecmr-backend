/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.openlogisticsfoundation.ecmr.api.model.SealedDocument;
import org.openlogisticsfoundation.ecmr.domain.exceptions.NoPermissionException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.SealedDocumentNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.ValidationException;
import org.openlogisticsfoundation.ecmr.domain.mappers.SealedDocumentPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.SealedDocumentWithoutEcmr;
import org.openlogisticsfoundation.ecmr.persistence.entities.SealedDocumentEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.SealedDocumentRepository;
import org.springframework.stereotype.Service;

import ecmr.seal.verify.dss.VerifierService;
import ecmr.seal.verify.rest.ESeal;
import ecmr.seal.verify.rest.SealVerifyResult;
import ecmr.seal.verify.rest.VerifyRequest;
import ecmr.seal.verify.rest.VerifyResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SealedDocumentService {

    private final VerifierService verifierService;
    private final SealedDocumentRepository sealedDocumentRepository;
    private final SealedDocumentPersistenceMapper sealedDocumentPersistenceMapper;
    private final AuthorisationService authorisationService;

    public SealedDocumentEntity saveSealedDocument(SealedDocumentEntity entity) {
        return this.sealedDocumentRepository.save(entity);
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
}
