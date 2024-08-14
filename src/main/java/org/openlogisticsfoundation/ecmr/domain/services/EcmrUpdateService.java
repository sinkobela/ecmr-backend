/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.openlogisticsfoundation.ecmr.api.model.EcmrConsignment;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.api.model.areas.twentyfour.GoodsReceived;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EcmrUpdateService {
    private final EcmrRepository ecmrRepository;
    private final EcmrPersistenceMapper persistenceMapper;
    private final Logger logger = LoggerFactory.getLogger(EcmrUpdateService.class);

    public EcmrModel changeType(UUID ecmrUuid, EcmrType ecmrType) throws EcmrNotFoundException {
        EcmrEntity ecmrEntity = ecmrRepository.findByEcmrId(ecmrUuid).orElseThrow(() -> new EcmrNotFoundException(ecmrUuid));
        ecmrEntity.setType(ecmrType);
        EcmrEntity result = this.ecmrRepository.save(ecmrEntity);
        return persistenceMapper.toModel(result);
    }

    public void archiveEcmrs() {
        List<EcmrEntity> entities = ecmrRepository.findAllByEcmrStatusAndType(EcmrStatus.ARRIVED_AT_DESTINATION, EcmrType.ECMR);
        logger.info("Archiving {} ECMRs", entities.size());
        for (EcmrEntity entity : entities) {
            entity.setType(EcmrType.ARCHIVED);
            this.ecmrRepository.save(entity);
        }
    }

    public EcmrModel updateEcmr(EcmrCommand ecmrCommand, UUID ecmrId, AuthenticatedUser authenticatedUser) throws EcmrNotFoundException {
        EcmrEntity ecmrEntity = ecmrRepository.findByEcmrId(ecmrId)
                .orElseThrow(() -> new EcmrNotFoundException(ecmrId));

        ecmrEntity = persistenceMapper.toEntity(ecmrEntity, ecmrCommand, EcmrType.ECMR);
        ecmrEntity.setEditedAt(Instant.now());

        String fullName = String.format("%s %s", authenticatedUser.getUser().getFirstName(), authenticatedUser.getUser().getLastName());
        ecmrEntity.setEditedBy(fullName);

        ecmrEntity = ecmrRepository.save(ecmrEntity);
        return persistenceMapper.toModel(ecmrEntity);
    }

    public boolean checkIfUpdatesAreValid(EcmrModel model) {
        EcmrConsignment consignment = model.getEcmrConsignment();
        boolean senderSigned = consignment.getSignatureOrStampOfTheSender().getSenderSignature() != null;
        boolean carrierSigned = consignment.getSignatureOrStampOfTheCarrier().getCarrierSignature() != null;
        // signature or stamp of the carrier: only when sender has signed
        if (carrierSigned && !senderSigned) {
            return false;
        }
        // carriers reservations and observations on taking over the goods : only when sender signed and carrier didn't
        else if (consignment.getCarriersReservationsAndObservationsOnTakingOverTheGoods().getCarrierReservationsObservations() != null
                && !(senderSigned && !carrierSigned)) {
            return false;
        }
        // goods received: only when both sender and carrier signed
        else if (goodsReceivedIsSet(consignment.getGoodsReceived()) && !(senderSigned && carrierSigned)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean goodsReceivedIsSet(GoodsReceived goodsReceived) {
        return Stream.of(
                        goodsReceived.getConfirmedLogisticsLocationName(),
                        goodsReceived.getConsigneeReservationsObservations(),
                        goodsReceived.getConsigneeTimeOfArrival(),
                        goodsReceived.getConsigneeTimeOfDeparture())
                .anyMatch(Objects::nonNull);
    }

}
