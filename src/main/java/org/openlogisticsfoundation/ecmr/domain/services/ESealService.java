/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import ecmr.seal.sign.dss.SignerService;
import ecmr.seal.verify.dss.VerifierService;
import ecmr.seal.verify.rest.ESeal;
import ecmr.seal.verify.rest.SealVerifyResult;
import ecmr.seal.verify.rest.VerifyRequest;
import ecmr.seal.verify.rest.VerifyResponse;
import org.openlogisticsfoundation.ecmr.persistence.entities.SealedDocumentEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.SealedEcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.SealedDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ESealService {

    private final SignerService signerService;
    private final VerifierService verifierService;
    private final SealedDocumentRepository sealedDocumentRepository;

    public ESealService( SignerService signerService, VerifierService verifierService, SealedDocumentRepository sealedDocumentRepository) {
        this.signerService = signerService;
        this.verifierService = verifierService;
        this.sealedDocumentRepository = sealedDocumentRepository;
    }

    public String sealEcmr(SealedEcmrEntity sealedEcmr, String precedingSeal) {
        // 1. seal ecmr
        Map<String, Object> claims = new HashMap<>();
        claims.put("ecmr", sealedEcmr);
        String seal = signerService.sign(claims, precedingSeal);

        // 2. save as sealedDocument
        SealedDocumentEntity sealedDocumentEntity = new SealedDocumentEntity();
        sealedDocumentEntity.setSeal(seal);
        sealedDocumentEntity.setSealedEcmr(sealedEcmr);

        sealedDocumentRepository.save(sealedDocumentEntity);

        return seal;
    }

    public SealVerifyResult verify(List<ESeal> seals) {
        VerifyRequest request = new VerifyRequest(seals, null, false, false, false, false);
        VerifyResponse result = verifierService.verify(request);
        return result.getResult();
    }
}
