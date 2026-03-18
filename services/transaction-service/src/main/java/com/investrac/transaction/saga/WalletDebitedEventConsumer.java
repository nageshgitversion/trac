package com.investrac.transaction.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investrac.common.events.TransactionCompletedEvent;
import com.investrac.common.events.TransactionFailedEvent;
import com.investrac.common.events.WalletDebitedEvent;
import com.investrac.transaction.entity.SagaProcessedEvent;
import com.investrac.transaction.outbox.TransactionOutboxService;
import com.investrac.transaction.repository.SagaProcessedEventRepository;
import com.investrac.transaction.repository.TransactionRepository;
import com.investrac.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * SAGA Step 3 — Transaction Service receives wallet result
 *
 * Listens to:  investrac.wallet.debited
 * Publishes:   investrac.transaction.completed  (if wallet success)
 *              investrac.transaction.failed      (if wallet failure)
 *
 * IDEMPOTENCY: sagaId checked against saga_processed_events table.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WalletDebitedEventConsumer {

    private final TransactionService           transactionService;
    private final TransactionRepository        transactionRepository;
    private final TransactionOutboxService     outboxService;
    private final SagaProcessedEventRepository sagaProcessedRepo;
    private final ObjectMapper                 objectMapper;

    @KafkaListener(
        topics     = WalletDebitedEvent.TOPIC,
        groupId    = "${spring.kafka.consumer.group-id:transaction-service-group}",
        concurrency = "3"
    )
    @Transactional
    public void onWalletDebited(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            WalletDebitedEvent event = objectMapper.readValue(
                record.value(), WalletDebitedEvent.class);

            log.info("SAGA Step 3: sagaId={} success={}", event.sagaId(), event.success());

            // ── IDEMPOTENCY CHECK ──
            if (sagaProcessedRepo.existsBySagaId(event.sagaId())) {
                log.warn("SAGA already processed (idempotency): sagaId={}", event.sagaId());
                ack.acknowledge();
                return;
            }

            // ── Fetch transaction for notification context ──
            var txOpt = transactionRepository.findBySagaId(event.sagaId());

            if (event.success()) {
                // Mark transaction COMPLETED
                transactionService.markCompleted(event.sagaId());

                // Publish completed event for notification-service
                txOpt.ifPresent(tx ->
                    outboxService.publish(TransactionCompletedEvent.TOPIC,
                        new TransactionCompletedEvent(
                            event.sagaId(),
                            tx.getId(),
                            tx.getUserId(),
                            tx.getAmount(),
                            tx.getName(),
                            tx.getCategory(),
                            Instant.now()
                        )
                    )
                );

                log.info("SAGA COMPLETED: sagaId={} txId={}",
                    event.sagaId(), txOpt.map(t -> t.getId()).orElse(null));

            } else {
                // Mark transaction FAILED
                transactionService.markFailed(event.sagaId(), event.failureReason());

                // Publish failed event for notification-service
                txOpt.ifPresent(tx ->
                    outboxService.publish(TransactionFailedEvent.TOPIC,
                        new TransactionFailedEvent(
                            event.sagaId(),
                            tx.getId(),
                            tx.getUserId(),
                            tx.getAmount(),
                            event.failureReason(),
                            event.failureCode(),
                            Instant.now()
                        )
                    )
                );

                log.warn("SAGA FAILED: sagaId={} reason={}", event.sagaId(), event.failureReason());
            }

            // ── MARK PROCESSED ──
            sagaProcessedRepo.save(SagaProcessedEvent.builder()
                .sagaId(event.sagaId())
                .eventType("WALLET_DEBITED")
                .result(event.success() ? "COMPLETED" : "FAILED")
                .build());

            ack.acknowledge();

        } catch (Exception e) {
            log.error("Error processing WalletDebitedEvent: {}", e.getMessage(), e);
            // Do NOT ack — Kafka will retry. After max retries → DLT
        }
    }
}
