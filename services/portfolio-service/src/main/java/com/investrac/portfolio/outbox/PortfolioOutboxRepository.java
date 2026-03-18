package com.investrac.portfolio.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PortfolioOutboxRepository extends JpaRepository<PortfolioOutboxEvent, Long> {
    List<PortfolioOutboxEvent> findByStatusAndRetryCountLessThan(
        PortfolioOutboxEvent.Status status, int maxRetries);
}
