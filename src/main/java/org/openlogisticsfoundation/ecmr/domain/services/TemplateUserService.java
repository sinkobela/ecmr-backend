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

import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.exceptions.TemplateUserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.exceptions.UserNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.mappers.TemplateUserPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.AuthenticatedUser;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.domain.models.TemplateUser;
import org.openlogisticsfoundation.ecmr.domain.models.User;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.TemplateUserCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.TemplateUserEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.TemplateUserRepository;
import org.openlogisticsfoundation.ecmr.persistence.repositories.UserRepository;
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
    private final UserRepository userRepository;
    private final UserService userService;

    public List<TemplateUser> getTemplatesForCurrentUser(AuthenticatedUser authenticatedUser) {
        return templateUserRepository.findAllByUserId(authenticatedUser.getUser().getId()).stream().map(templateUserPersistenceMapper::toTemplateUser).toList();
    }

    public TemplateUser getTemplateForCurrentUser(AuthenticatedUser authenticatedUser, Long id) throws TemplateUserNotFoundException {
        TemplateUserEntity templateUserEntity =
                templateUserRepository.findByIdAndUserId(id, authenticatedUser.getUser().getId()).orElseThrow(() -> new TemplateUserNotFoundException(id));
        return templateUserPersistenceMapper.toTemplateUser(templateUserEntity);
    }

    public TemplateUser createTemplate(EcmrCommand ecmrCommand, String name, AuthenticatedUser authenticatedUser) throws UserNotFoundException {
        EcmrEntity ecmr = removeFields(ecmrCreationService.createTemplate(ecmrCommand, authenticatedUser));
        TemplateUserEntity templateUser = createNewTemplateForUser(ecmr, name, authenticatedUser);
        return templateUserPersistenceMapper.toTemplateUser(templateUser);
    }

    private TemplateUserEntity createNewTemplateForUser(EcmrEntity ecmr, String name, AuthenticatedUser user) throws UserNotFoundException {
        TemplateUserEntity templateUser = new TemplateUserEntity();
        templateUser.setUser(userRepository.findById(user.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException(user.getUser().getId())));

        String fullName = String.format("%s %s", user.getUser().getFirstName(), user.getUser().getLastName());
        ecmr.setCreatedBy(fullName);
        ecmr.setCreatedAt(Instant.now());

        templateUser.setEcmr(ecmr);
        templateUser.setName(name);
        int maxTemplateUserNumber = templateUserRepository.findMaxTemplateNumberForUser(ecmr.getCreatedBy()) == null ? 0 :
                templateUserRepository.findMaxTemplateNumberForUser(ecmr.getCreatedBy());
        templateUser.setTemplateUserNumber(maxTemplateUserNumber + 1);

        return templateUserRepository.save(templateUser);
    }

    public void shareTemplate(Long id, List<Long> userIdsToShareWith) throws TemplateUserNotFoundException, UserNotFoundException {
        TemplateUserEntity templateUserEntity = templateUserRepository.findById(id).orElseThrow(() -> new TemplateUserNotFoundException(id));
        EcmrCommand ecmrCommand = ecmrWebMapper.toCommand(ecmrPersistenceMapper.toModel(templateUserEntity.getEcmr()));

        for (Long userIdToShareWith : userIdsToShareWith) {
            AuthenticatedUser authUserToShareWith = new AuthenticatedUser(userService.getActiveUserById(userIdToShareWith));
            EcmrEntity newEcmr = removeFields(ecmrCreationService.createTemplate(ecmrCommand, authUserToShareWith));
            createNewTemplateForUser(newEcmr, templateUserEntity.getName(), authUserToShareWith);
        }
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

    public void deleteTemplate(Long id) throws TemplateUserNotFoundException {
        TemplateUserEntity templateUserEntity = templateUserRepository.findById(id).orElseThrow(() -> new TemplateUserNotFoundException(id));
        EcmrEntity ecmrEntity = templateUserEntity.getEcmr();

        templateUserRepository.delete(templateUserEntity);
        ecmrRepository.delete(ecmrEntity);
    }
}
