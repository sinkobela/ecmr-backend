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
import java.util.UUID;

import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.api.model.signature.Signature;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.SignatureAlreadyPresentException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.SignatureNotValidException;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.domain.models.SignatureType;
import org.openlogisticsfoundation.ecmr.domain.models.Signer;
import org.openlogisticsfoundation.ecmr.domain.models.commands.SignCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.SignatureEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EcmrService {
    private final EcmrRepository ecmrRepository;
    private final EcmrPersistenceMapper ecmrPersistenceMapper;
    private final GroupService groupService;

    public EcmrModel getEcmr(UUID ecmrId) throws EcmrNotFoundException {
        EcmrEntity ecmrEntity = ecmrRepository.findByEcmrId(ecmrId).orElseThrow(() -> new EcmrNotFoundException(ecmrId));
        return ecmrPersistenceMapper.toModel(ecmrEntity);
    }

    EcmrEntity getEcmrEntity(UUID ecmrId) throws EcmrNotFoundException {
        return ecmrRepository.findByEcmrId(ecmrId).orElseThrow(() -> new EcmrNotFoundException(ecmrId));
    }

    public List<EcmrModel> getEcmrsForUser(AuthenticatedUser authenticatedUser, EcmrType ecmrType, int page, int size, String sortBy, String sortingOrder) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortingOrder);
        final Pageable pageable = PageRequest.of(page, size, sortDirection, "ecmr." + sortBy);

        List<Group> usersGroups = groupService.getGroupsForUser(authenticatedUser);
        List<Long> usersGroupIds = groupService.flatMapGroupTrees(usersGroups).stream().map(Group::getId).toList();

        final Page<EcmrEntity> ecmrPage = ecmrRepository.findAllByTypeAndAssignedGroupIds(ecmrType, usersGroupIds, pageable);

        return ecmrPage.get()
                .map(ecmrPersistenceMapper::toModel)
                .toList();
    }

    public void deleteEcmr(UUID ecmrId) throws EcmrNotFoundException {
        EcmrEntity ecmrEntity = ecmrRepository.findByEcmrId(ecmrId).orElseThrow(() -> new EcmrNotFoundException(ecmrId));

        if (ecmrEntity != null && ecmrEntity.getCarrierInformation().getSignature() == null
                && ecmrEntity.getSenderInformation().getSignature() == null
                && ecmrEntity.getConsigneeInformation().getSignature() == null && ecmrEntity.getSuccessiveCarrierInformation().getSignature() == null
                && ecmrEntity.getEcmrStatus() == EcmrStatus.NEW) {
            ecmrRepository.delete(ecmrEntity);
        }
    }

    public Signature signEcmr(AuthenticatedUser authenticatedUser, UUID ecmrId, SignCommand signCommand, SignatureType signatureType)
            throws EcmrNotFoundException, SignatureAlreadyPresentException, SignatureNotValidException {
        EcmrEntity ecmrEntity = ecmrRepository.findByEcmrId(ecmrId).orElseThrow(() -> new EcmrNotFoundException(ecmrId));
        SignatureEntity signatureEntity = new SignatureEntity();
        signatureEntity.setData(signCommand.getData());
        signatureEntity.setTimestamp(Instant.now());
        signatureEntity.setUserName(authenticatedUser.getUser().getFirstName() + " " + authenticatedUser.getUser().getLastName());
        signatureEntity.setUserCountry(authenticatedUser.getUser().getCountry().name());
        signatureEntity.setSignatureType(signatureType);

        switch (signCommand.getSigner()) {
        case Signer.Sender -> {
            if (ecmrEntity.getSenderInformation().getSignature() != null) {
                throw new SignatureAlreadyPresentException(ecmrId, signCommand.getSigner().name());
            }
            ecmrEntity.getSenderInformation().setSignature(signatureEntity);
        }
        case Signer.Carrier -> {
            if (ecmrEntity.getCarrierInformation().getSignature() != null) {
                throw new SignatureAlreadyPresentException(ecmrId, signCommand.getSigner().name());
            }
            ecmrEntity.getCarrierInformation().setSignature(signatureEntity);
        }
        case Signer.Consignee -> {
            if (ecmrEntity.getConsigneeInformation().getSignature() != null) {
                throw new SignatureAlreadyPresentException(ecmrId, signCommand.getSigner().name());
            }
            ecmrEntity.getConsigneeInformation().setSignature(signatureEntity);
        }
        default -> throw new SignatureNotValidException(ecmrId, "type not valid: " + signCommand.getSigner().name());
        }

        this.ecmrRepository.save(ecmrEntity);

        Signature signatureModel = new Signature();
        ecmrPersistenceMapper.signatureEntityToSignature(signatureEntity,signatureModel);

        return signatureModel;
    }
}
