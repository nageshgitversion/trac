package com.investrac.wallet.repository;

import com.investrac.wallet.entity.SagaProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaProcessedEventRepository extends JpaRepository<SagaProcessedEvent, Long> {
    boolean existsBySagaId(String sagaId);
}
