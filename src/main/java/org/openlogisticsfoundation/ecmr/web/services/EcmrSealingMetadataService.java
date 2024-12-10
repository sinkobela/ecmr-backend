/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.web.services;

import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrSealingMetadataEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.SealedDocumentEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrSealingMetadataRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.SealedDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EcmrSealingMetadataService {

    private final SealedDocumentRepository sealedDocumentRepository;

    @Autowired
    public EcmrSealingMetadataService(SealedDocumentRepository sealedDocumentRepository) {
        this.sealedDocumentRepository = sealedDocumentRepository;
    }

    public EcmrSealingMetadataEntity createMetadata(String sealer, Instant timestamp, SealedDocumentEntity sealedDocument) {
        EcmrSealingMetadataEntity metadata = new EcmrSealingMetadataEntity();
        metadata.setSealer(sealer);
        metadata.setTimestamp(timestamp);
        sealedDocument.getSealedEcmr().setMetadata(metadata);

        sealedDocumentRepository.save(sealedDocument);

        return sealedDocument.getSealedEcmr().getMetadata();
    }
}
