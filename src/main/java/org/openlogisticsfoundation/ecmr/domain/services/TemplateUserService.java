/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.List;

import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.exceptions.TemplateUserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.mappers.TemplateUserPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.TemplateUser;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.TemplateUserCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.TemplateUserEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.TemplateUserRepository;
import org.openlogisticsfoundation.ecmr.web.mappers.EcmrWebMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateUserService {
    private final EcmrRepository ecmrRepository;
    private final EcmrPersistenceMapper ecmrPersistenceMapper;
    private final EcmrCreationService ecmrCreationService;
    private final EcmrWebMapper ecmrWebMapper;
    private final TemplateUserRepository templateUserRepository;
    private final TemplateUserPersistenceMapper templateUserPersistenceMapper;

    public List<TemplateUser> getTemplatesForCurrentUser() {
        //TODO: find by authenticated user, not Test User ->
        return templateUserRepository.findAllByEcmr_CreatedBy("Test User").stream().map(templateUserPersistenceMapper::toTemplateUser).toList();
    }

    public TemplateUser getTemplateForCurrentUser(Long id) throws TemplateUserNotFoundException {
        //TODO: find by authenticated user, not Test User ->
        TemplateUserEntity templateUserEntity =
                templateUserRepository.findByIdAndEcmr_CreatedBy(id, "Test User").orElseThrow(() -> new TemplateUserNotFoundException(id));
        return templateUserPersistenceMapper.toTemplateUser(templateUserEntity);
    }

    public TemplateUser createTemplate(EcmrCommand ecmrCommand, String name) {
        EcmrEntity ecmr = removeFields(ecmrCreationService.createTemplate(ecmrCommand));
        TemplateUserEntity templateUser = new TemplateUserEntity();
        //TODO: set createdBy to authenticated user, not Test User ->
        ecmr.setCreatedBy("Test User");
        templateUser.setEcmr(ecmr);
        templateUser.setName(name);
        //TODO: Check for authenticated user, not Test User ->
        int maxTemplateUserNumber = templateUserRepository.findMaxTemplateNumberForUser(ecmr.getCreatedBy()) == null ? 0 :
                templateUserRepository.findMaxTemplateNumberForUser(ecmr.getCreatedBy());
        templateUser.setTemplateUserNumber(maxTemplateUserNumber + 1);
        return templateUserPersistenceMapper.toTemplateUser(templateUserRepository.save(templateUser));
    }

    private EcmrEntity removeFields(EcmrEntity ecmr) {
        ecmr.getConsigneeInformation().setSignature(null);
        ecmr.getCarrierInformation().setSignature(null);
        ecmr.getConsigneeInformation().setSignature(null);
        ecmr.getItemList().clear();
        return ecmr;
    }

    public TemplateUser updateTemplate(TemplateUserCommand templateUserCommand) throws TemplateUserNotFoundException {
        TemplateUserEntity templateUserEntity = templateUserRepository.findById(templateUserCommand.getId())
                .orElseThrow(() -> new TemplateUserNotFoundException(templateUserCommand.getId()));

        EcmrEntity ecmrEntity = ecmrPersistenceMapper.toEntity(templateUserCommand.getEcmr(), EcmrType.TEMPLATE, EcmrStatus.NEW);
        ecmrEntity.setId(templateUserEntity.getEcmr().getId());
        ecmrEntity.setEcmrId(templateUserEntity.getEcmr().getEcmrId());
        ecmrEntity.setCreatedBy(templateUserEntity.getEcmr().getCreatedBy());

        templateUserEntity.setEcmr(ecmrEntity);

        return templateUserPersistenceMapper.toTemplateUser(templateUserRepository.save(templateUserEntity));
    }

    public void shareTemplate(Long id, List<Long> userIdsToShareWith) throws TemplateUserNotFoundException {
        TemplateUserEntity templateUserEntity = templateUserRepository.findById(id).orElseThrow(() -> new TemplateUserNotFoundException(id));
        //TODO: Check if authenticated user is creator of Template ->
        for (Long userIdToShareWith : userIdsToShareWith) {
            templateUserEntity.getEcmr().setCreatedBy(userIdToShareWith.toString());
            EcmrCommand ecmrCommand = ecmrWebMapper.toCommand(ecmrPersistenceMapper.toModel(templateUserEntity.getEcmr()));
            createTemplate(ecmrCommand, templateUserEntity.getName());
        }
    }

    public void deleteTemplate(Long id) throws TemplateUserNotFoundException {
        TemplateUserEntity templateUserEntity = templateUserRepository.findById(id).orElseThrow(() -> new TemplateUserNotFoundException(id));
        EcmrEntity ecmrEntity = templateUserEntity.getEcmr();

        templateUserRepository.delete(templateUserEntity);
        ecmrRepository.delete(ecmrEntity);
    }
}
