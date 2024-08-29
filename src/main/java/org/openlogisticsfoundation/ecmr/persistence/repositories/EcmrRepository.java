/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.persistence.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.openlogisticsfoundation.ecmr.api.model.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EcmrRepository extends JpaRepository<EcmrEntity, Long> {
    @EntityGraph(value = "Ecmr.all", type = EntityGraph.EntityGraphType.FETCH)
    Optional<EcmrEntity> findByEcmrId(UUID ecmrId);

    @EntityGraph(value = "Ecmr.all", type = EntityGraph.EntityGraphType.FETCH)
    List<EcmrEntity> findAllByEcmrStatusAndType(EcmrStatus ecmrStatus, EcmrType type);

    @EntityGraph(value = "Ecmr.all", type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT Distinct e FROM EcmrEntity e "
            + "WHERE e.type = :type "
            + "AND (:referenceId is null or e.referenceIdentificationNumber LIKE %:referenceId%) "
            + "AND (:from is null or e.senderInformation.nameCompany LIKE %:from%)"
            + "AND (:to is null or e.consigneeInformation.nameCompany LIKE %:to%)"
            + "AND (:ecmrStatus is null or e.ecmrStatus = :ecmrStatus)"
            + "AND (:licensePlate is null or e.carrierInformation.carrierLicensePlate LIKE %:licensePlate%)"
            + "AND (:carrierName is null or e.carrierInformation.nameCompany LIKE %:carrierName%)"
            + "AND (:carrierPostCode is null or e.carrierInformation.postcode LIKE %:carrierPostCode%)"
            + "AND (:consigneePostCode is null or e.consigneeInformation.postcode LIKE %:consigneePostCode%)"
            + "AND (:ecmrTransportType is null OR "
            + "((:ecmrTransportType = 'National' AND e.senderInformation.countryCode = e.consigneeInformation.countryCode)"
            + "OR (:ecmrTransportType = 'International' AND e.senderInformation.countryCode != e.consigneeInformation.countryCode)))"
            + "AND e.id IN (SELECT ea.ecmr.id FROM EcmrAssignmentEntity ea WHERE ea.group.id in :groupIds)")
    Page<EcmrEntity> findAllByTypeAndAssignedGroupIds(@Param("type") EcmrType type, @Param("groupIds") List<Long> groupIds,
            @Param("referenceId") String referenceId, @Param("from") String from, @Param("to") String to,
            @Param("ecmrTransportType") String ecmrTransportType, @Param("ecmrStatus") EcmrStatus ecmrStatus,
            @Param("licensePlate") String licensePlate, @Param("carrierName") String carrierName, @Param("carrierPostCode") String carrierPostCode,
            @Param("consigneePostCode") String consigneePostCode,
            Pageable pageable);

    boolean existsByEcmrId(UUID ecmrId);
}
