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
