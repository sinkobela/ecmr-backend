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
import org.mapstruct.Named;
import org.openlogisticsfoundation.ecmr.api.model.EcmrSeal;
import org.openlogisticsfoundation.ecmr.api.model.SealedDocument;
import org.openlogisticsfoundation.ecmr.domain.models.SealedDocumentWithoutEcmr;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrSealEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.SealedDocumentEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.SealedDocumentProjection;

@Mapper(componentModel = "spring", uses = EcmrPersistenceMapper.class)
public interface SealedDocumentPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senderSeal", qualifiedByName = "mapToSealEntity")
    @Mapping(target = "carrierSeal", qualifiedByName = "mapToSealEntity")
    @Mapping(target = "successiveCarrierSeal", qualifiedByName = "mapToSealEntity")
    @Mapping(target = "consigneeSeal", qualifiedByName = "mapToSealEntity")
    SealedDocumentEntity toEntity(SealedDocument sealedDocument);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "last_updated", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "sealer", source = "sealMetadata.sealer")
    @Mapping(target = "transportRole", source = "sealMetadata.role")
    @Mapping(target = "timestamp", source = "sealMetadata.timestamp")
    @Named("mapToSealEntity")
    EcmrSealEntity toSealEntity(EcmrSeal ecmrSeal);

    @Mapping(target = "sealMetadata.sealer", source = "sealer")
    @Mapping(target = "sealMetadata.role", source = "transportRole")
    @Mapping(target = "sealMetadata.timestamp", source = "timestamp")
    @Named("mapToSeal")
    EcmrSeal toSeal(EcmrSealEntity ecmrSeal);

    @Mapping(target = "senderSeal", qualifiedByName = "mapToSeal")
    @Mapping(target = "carrierSeal", qualifiedByName = "mapToSeal")
    @Mapping(target = "successiveCarrierSeal", qualifiedByName = "mapToSeal")
    @Mapping(target = "consigneeSeal", qualifiedByName = "mapToSeal")
    SealedDocument toDomain(SealedDocumentEntity sealedDocumentEntity);

    @Mapping(target = "senderSeal", qualifiedByName = "mapToSeal")
    @Mapping(target = "carrierSeal", qualifiedByName = "mapToSeal")
    @Mapping(target = "successiveCarrierSeal", qualifiedByName = "mapToSeal")
    @Mapping(target = "consigneeSeal", qualifiedByName = "mapToSeal")
    SealedDocumentWithoutEcmr toDomainWithoutEcmr(SealedDocumentProjection sealedDocumentProjection);
}
