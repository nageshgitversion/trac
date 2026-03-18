package com.investrac.wallet.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WalletOutboxRepository extends JpaRepository<WalletOutboxEvent, Long> {
    List<WalletOutboxEvent> findByStatusAndRetryCountLessThan(WalletOutboxEvent.Status status, int maxRetries);
}
