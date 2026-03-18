package com.investrac.transaction.repository;

import com.investrac.transaction.entity.SagaProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaProcessedEventRepository extends JpaRepository<SagaProcessedEvent, Long> {
    boolean existsBySagaId(String sagaId);
}
