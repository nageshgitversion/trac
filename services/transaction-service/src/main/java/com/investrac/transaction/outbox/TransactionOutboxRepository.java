package com.investrac.transaction.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionOutboxRepository extends JpaRepository<TransactionOutboxEvent, Long> {
    List<TransactionOutboxEvent> findByStatusAndRetryCountLessThan(
        TransactionOutboxEvent.Status status, int maxRetries);
}
