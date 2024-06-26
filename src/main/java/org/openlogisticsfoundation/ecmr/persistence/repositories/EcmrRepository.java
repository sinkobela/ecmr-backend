package org.openlogisticsfoundation.ecmr.persistence.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.openlogisticsfoundation.ecmr.domain.models.EcmrType;
import org.openlogisticsfoundation.ecmr.persistence.entities.EcmrEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EcmrRepository extends JpaRepository<EcmrEntity, Long> {
    @EntityGraph(value = "Ecmr.all", type = EntityGraph.EntityGraphType.FETCH)
    Optional<EcmrEntity> findByEcmrId(UUID ecmrId);

    @EntityGraph(value = "Ecmr.all", type = EntityGraph.EntityGraphType.FETCH)
    List<EcmrEntity> findAllByType(EcmrType type);
}

