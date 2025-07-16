/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */
package org.openlogisticsfoundation.ecmr.domain.services;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrRole;
import org.openlogisticsfoundation.ecmr.domain.models.Group;
import org.openlogisticsfoundation.ecmr.domain.models.InternalOrExternalUser;
import org.openlogisticsfoundation.ecmr.domain.models.commands.CustomChargeCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.DeliveryOfTheGoodsCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.EcmrMemberCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.GoodsReceivedCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ItemCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.LogisticsShippingMarksCustomBarcodeCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.TakingOverTheGoodsCommand;
import org.openlogisticsfoundation.ecmr.domain.models.commands.ToBePaidByCommand;
import org.openlogisticsfoundation.ecmr.persistence.entities.CustomChargeEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.DeliveryOfTheGoodsEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrAssignmentEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrMemberEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.GoodsReceivedEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.ItemEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.LogisticsShippingMarksCustomBarcodeEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.TakingOverTheGoodsEntity;
import org.openlogisticsfoundation.ecmr.persistence.entities.ToBePaidByEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorisationService {

    private final EcmrAssignmentRepository assignmentRepository;
    private final GroupService groupService;

    public boolean tanValid(UUID ecmrId, String userToken, String tan) {
        return !assignmentRepository.findByExternalUser(ecmrId, userToken, tan).isEmpty();
    }

    @Transactional
    public boolean doesNotHaveRole(InternalOrExternalUser internalOrExternalUser, UUID ecmrId, EcmrRole role) {
        List<EcmrRole> rolesOfUser = this.getRolesOfUser(internalOrExternalUser, ecmrId);
        return !rolesOfUser.contains(role);
    }

    @Transactional
    public boolean hasNoRole(InternalOrExternalUser internalOrExternalUser, UUID ecmrId) {
        List<EcmrRole> rolesOfUser = this.getRolesOfUser(internalOrExternalUser, ecmrId);
        return rolesOfUser.isEmpty();
    }

    @Transactional
    public boolean validateSaveCommand(EcmrCommand ecmrCommandToSave) {
        return StringUtils.isBlank(ecmrCommandToSave.getNonContractualCarrierRemarks()) && StringUtils.isBlank(
                ecmrCommandToSave.getCarrierReservationsObservations());
    }

    @Transactional
    public boolean validateUpdateCommand(EcmrCommand ecmrToChange, EcmrEntity ecmrEntity, InternalOrExternalUser internalOrExternalUser) {
        List<EcmrRole> rolesOfUser = this.getRolesOfUser(internalOrExternalUser, ecmrEntity.getEcmrId());
        return validateUpdateCommand(ecmrToChange, ecmrEntity, rolesOfUser);
    }

    @Transactional
    public List<EcmrRole> getRolesOfUser(InternalOrExternalUser internalOrExternalUser, UUID ecmrId) {
        if (internalOrExternalUser.isInternalUser()) {
            return getRolesOfInternalUser(internalOrExternalUser.getInternalUser().getId(), ecmrId);
        } else {
            return getRolesOfExternalUser(internalOrExternalUser.getExternalUser().getUserToken(), internalOrExternalUser.getExternalUser().getTan(), ecmrId);
        }
    }

    private List<EcmrRole> getRolesOfInternalUser(long userId, UUID ecmrId) {
        List<Group> usersGroups = groupService.getGroupsForUser(userId);
        List<Long> groupIds = groupService.flatMapGroupTrees(usersGroups).stream().map(Group::getId).toList();
        return this.assignmentRepository.findByEcmr_EcmrIdAndGroup_IdIn(ecmrId, groupIds).stream()
                .map(EcmrAssignmentEntity::getRole).toList();
    }

    private List<EcmrRole> getRolesOfExternalUser(String userToken, String tan, UUID ecmrId) {
        return this.assignmentRepository.findByExternalUser(ecmrId, userToken, tan)
                .stream()
                .map(EcmrAssignmentEntity::getRole).toList();
    }

    private boolean validateUpdateCommand(EcmrCommand ecmrToChange, EcmrEntity ecmrEntity, List<EcmrRole> rolesOfUser) {
        if(ecmrEntity.getEcmrStatus() != EcmrStatus.NEW &&
                !ecmrEntity.getReferenceIdentificationNumber().equals(ecmrToChange.getReferenceIdentificationNumber())) {
            return false;
        }
        if ((!rolesOfUser.contains(EcmrRole.Sender) || ecmrEntity.getEcmrStatus() != EcmrStatus.NEW)
                && this.checkSenderFieldsChanged(ecmrToChange, ecmrEntity)) {
            return false;
        }
        if ((!rolesOfUser.contains(EcmrRole.Carrier) || ecmrEntity.getEcmrStatus() != EcmrStatus.LOADING)
                && (!Objects.equals(ecmrToChange.getNonContractualCarrierRemarks(), ecmrEntity.getNonContractualCarrierRemarks())
                || !Objects.equals(ecmrToChange.getCarrierReservationsObservations(), ecmrEntity.getCarrierReservationsObservations()))) {
            return false;
        }
        if ((!rolesOfUser.contains(EcmrRole.Consignee) || ecmrEntity.getEcmrStatus() != EcmrStatus.IN_TRANSPORT)
                && this.checkGoodsReceivedChanged(ecmrToChange.getGoodsReceived(), ecmrEntity.getGoodsReceived())) {
            return false;
        }
        return true;
    }

    private boolean checkSenderFieldsChanged(EcmrCommand ecmrToChange, EcmrEntity ecmrEntity) {
        return this.checkEcmrMemberChanged(ecmrToChange.getSenderInformation(), ecmrEntity.getSenderInformation())
                || this.checkTakingOverGoodsChanged(ecmrToChange.getTakingOverTheGoods(), ecmrEntity.getTakingOverTheGoods())
                || !Objects.equals(ecmrToChange.getTransportInstructionsDescription(), ecmrEntity.getTransportInstructionsDescription())
                || this.checkEcmrMemberChanged(ecmrToChange.getCarrierInformation(), ecmrEntity.getCarrierInformation())
                || !Objects.equals(ecmrToChange.getCarrierInformation().getDriverPhone(), ecmrEntity.getCarrierInformation().getDriverPhone())
                || !Objects.equals(ecmrToChange.getCarrierInformation().getCarrierLicensePlate(), ecmrEntity.getCarrierInformation().getCarrierLicensePlate())
                || this.checkDeliveryOfTheGoodsChanged(ecmrToChange.getDeliveryOfTheGoods(), ecmrEntity.getDeliveryOfTheGoods())
                || this.checkItemsChanged(ecmrToChange.getItemList(), ecmrEntity.getItemList())
                || !Objects.equals(ecmrToChange.getCustomSpecialAgreement(), ecmrEntity.getCustomSpecialAgreement())
                || this.checkToBePaidChanged(ecmrToChange.getToBePaidBy(), ecmrEntity.getToBePaidBy())
                || !Objects.equals(ecmrToChange.getCustomParticulars(), ecmrEntity.getCustomParticulars())
                || !Objects.equals(ecmrToChange.getCustomCashOnDelivery(), ecmrEntity.getCustomCashOnDelivery())
                || !Objects.equals(ecmrToChange.getCustomEstablishedDate(), ecmrEntity.getCustomEstablishedDate())
                || !Objects.equals(ecmrToChange.getCustomEstablishedIn(), ecmrEntity.getCustomEstablishedIn())
                || !Objects.equals(ecmrToChange.getReferenceIdentificationNumber(), ecmrEntity.getReferenceIdentificationNumber());
    }

    private boolean checkEcmrMemberChanged(EcmrMemberCommand command, EcmrMemberEntity entity) {
        return !Objects.equals(command.getCompanyName(), entity.getCompanyName())
                || !Objects.equals(command.getPersonName(), entity.getPersonName())
                || !Objects.equals(command.getStreet(), entity.getStreet())
                || !Objects.equals(command.getPostcode(), entity.getPostcode())
                || !Objects.equals(command.getCity(), entity.getCity())
                || !Objects.equals(command.getCountryCode(), entity.getCountryCode())
                || !Objects.equals(command.getEmail(), entity.getEmail())
                || !Objects.equals(command.getPhone(), entity.getPhone());
    }

    private boolean checkTakingOverGoodsChanged(TakingOverTheGoodsCommand command, TakingOverTheGoodsEntity entity) {
        return !Objects.equals(command.getTakingOverTheGoodsPlace(), entity.getTakingOverTheGoodsPlace())
                || !Objects.equals(command.getLogisticsTimeOfArrivalDateTime(), entity.getLogisticsTimeOfArrivalDateTime())
                || !Objects.equals(command.getLogisticsTimeOfDepartureDateTime(), entity.getLogisticsTimeOfDepartureDateTime());
    }

    private boolean checkDeliveryOfTheGoodsChanged(DeliveryOfTheGoodsCommand command, DeliveryOfTheGoodsEntity entity) {
        return !Objects.equals(command.getLogisticsLocationCity(), entity.getLogisticsLocationCity())
                || !Objects.equals(command.getLogisticsLocationOpeningHours(), entity.getLogisticsLocationOpeningHours());
    }

    private boolean checkItemsChanged(List<ItemCommand> commands, List<ItemEntity> entities) {
        if (commands.size() != entities.size()) {
            return false;
        }

        for (int i = 0; i < commands.size(); i++) {
            ItemCommand command = commands.get(i);
            ItemEntity entity = entities.get(i);

            List<String> commandBarcodesAsString = command.getLogisticsShippingMarksCustomBarcodeList().stream()
                            .map(LogisticsShippingMarksCustomBarcodeCommand::getBarcode)
                            .sorted().toList();
            List<String> entityBarcodesAsString = entity.getLogisticsShippingMarksCustomBarcodeList().stream()
                    .map(LogisticsShippingMarksCustomBarcodeEntity::getBarcode)
                    .sorted().toList();

            if(!Objects.equals(command.getLogisticsPackageType(), entity.getLogisticsPackageType())
            || !Objects.equals(command.getLogisticsPackageItemQuantity(), entity.getLogisticsPackageItemQuantity())
            || !Objects.equals(command.getSupplyChainConsignmentItemGrossWeight(),entity.getSupplyChainConsignmentItemGrossWeight())
            || !Objects.equals(command.getSupplyChainConsignmentItemGrossVolume(), entity.getSupplyChainConsignmentItemGrossVolume())
            || !Objects.equals(command.getLogisticsShippingMarksMarking(),  entity.getLogisticsShippingMarksMarking())
            || !Objects.equals(command.getTransportCargoIdentification(), entity.getTransportCargoIdentification())
            || !Objects.equals(commandBarcodesAsString.size(), entityBarcodesAsString.size())) {
                return true;
            }

            for (int j = 0; j < commandBarcodesAsString.size(); j++) {
                if (!commandBarcodesAsString.get(i).equals(entityBarcodesAsString.get(i))) {
                    return true;
                }
            }

        }
        return false;
    }

    private boolean checkToBePaidChanged(ToBePaidByCommand command, ToBePaidByEntity entity) {
        return this.checkCustomChargeChanged(command.getCustomChargeCarriage(), entity.getCustomChargeCarriage())
                || this.checkCustomChargeChanged(command.getCustomChargeSupplementary(), entity.getCustomChargeSupplementary())
                || this.checkCustomChargeChanged(command.getCustomChargeCustomsDuties(), entity.getCustomChargeCustomsDuties())
                || this.checkCustomChargeChanged(command.getCustomChargeOther(), entity.getCustomChargeOther());
    }

    private boolean checkCustomChargeChanged(CustomChargeCommand command, CustomChargeEntity entity) {
        return !Objects.equals(command.getValue(), entity.getValue())
                || !Objects.equals(command.getCurrency(), entity.getCurrency())
                || !Objects.equals(command.getPayer(), entity.getPayer());
    }

    private boolean checkGoodsReceivedChanged(GoodsReceivedCommand command, GoodsReceivedEntity entity) {
        return !Objects.equals(command.getConfirmedLogisticsLocationName(), entity.getConfirmedLogisticsLocationName())
                || !Objects.equals(command.getConsigneeReservationsObservations(), entity.getConsigneeReservationsObservations())
                || !Objects.equals(command.getConsigneeTimeOfArrival(), entity.getConsigneeTimeOfArrival())
                || !Objects.equals(command.getConsigneeTimeOfDeparture(), entity.getConsigneeTimeOfDeparture());
    }

}
