package com.investrac.auth.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByStatusAndRetryCountLessThan(OutboxEvent.OutboxStatus status, int maxRetries);
}
