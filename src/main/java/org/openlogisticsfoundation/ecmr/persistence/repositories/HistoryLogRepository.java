/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.persistence.repositories;

import java.util.List;
import java.util.UUID;

import org.openlogisticsfoundation.ecmr.persistence.entities.HistoryLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryLogRepository extends JpaRepository<HistoryLogEntity, Long> {
    List<HistoryLogEntity> findByEcmr_EcmrIdOrderByTimestampDesc(UUID ecmrId);
    void deleteAllByEcmr_EcmrId(UUID ecmrId);
}
