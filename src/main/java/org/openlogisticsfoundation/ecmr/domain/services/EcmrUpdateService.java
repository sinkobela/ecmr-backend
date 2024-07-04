package org.openlogisticsfoundation.ecmr.domain.services;

import lombok.AllArgsConstructor;
import org.openlogisticsfoundation.ecmr.api.model.EcmrModel;
import org.openlogisticsfoundation.ecmr.domain.exceptions.EcmrNotFoundException;
import org.openlogisticsfoundation.ecmr.domain.mappers.EcmrPersistenceMapper;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrStatus;
import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.openlogisticsfoundation.ecmr.persistence.repositories.EcmrRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    public void archiveEcmrs(){
        List<EcmrEntity> entities = ecmrRepository.findAllByEcmrStatusAndType(EcmrStatus.ARRIVED_AT_DESTINATION, EcmrType.ECMR);
        logger.info("Archiving {} ECMRs", entities.size());
        for(EcmrEntity entity : entities) {
            entity.setType(EcmrType.ARCHIVED);
            this.ecmrRepository.save(entity);
        }
    }

}
