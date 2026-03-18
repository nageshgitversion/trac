package com.investrac.account.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccountOutboxRepository extends JpaRepository<AccountOutboxEvent, Long> {
    List<AccountOutboxEvent> findByStatusAndRetryCountLessThan(
        AccountOutboxEvent.Status status, int maxRetries);
}
