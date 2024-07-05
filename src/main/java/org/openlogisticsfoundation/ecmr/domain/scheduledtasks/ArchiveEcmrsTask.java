/*
 * Copyright Open Logistics Foundation
 *
 * Licensed under the Open Logistics Foundation License 1.3.
 * For details on the licensing terms, see the LICENSE file.
 * SPDX-License-Identifier: OLFL-1.3
 */

package org.openlogisticsfoundation.ecmr.domain.scheduledtasks;

import lombok.AllArgsConstructor;
import org.openlogisticsfoundation.ecmr.domain.services.EcmrUpdateService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class ArchiveEcmrsTask {

    private final EcmrUpdateService ecmrUpdateService;

    @Scheduled(cron = "0 0 2 * * ?", zone = "UTC")
    public void archivingEcmrs(){
        ecmrUpdateService.archiveEcmrs();
    }
}
