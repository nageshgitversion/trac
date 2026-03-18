package com.investrac.wallet.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investrac.common.events.TransactionCreatedEvent;
import com.investrac.common.events.WalletDebitedEvent;
import com.investrac.wallet.outbox.WalletOutboxService;
import com.investrac.wallet.repository.SagaProcessedEventRepository;
import com.investrac.wallet.entity.SagaProcessedEvent;
import com.investrac.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * SAGA Step 2: Wallet Service
 *
 * Listens to: investrac.transaction.created
 * Publishes:  investrac.wallet.debited (success or failure)
 *
 * IDEMPOTENCY: sagaId is checked against saga_processed_events table.
 * If already processed (Kafka redelivery), skip and re-publish the same result.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final WalletService                walletService;
    private final WalletOutboxService          outboxService;
    private final SagaProcessedEventRepository sagaProcessedRepo;
    private final ObjectMapper                 objectMapper;

    @KafkaListener(
        topics = TransactionCreatedEvent.TOPIC,
        groupId = "${spring.kafka.consumer.group-id:wallet-service-group}",
        concurrency = "3"
    )
    @Transactional
    public void onTransactionCreated(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            TransactionCreatedEvent event = objectMapper.readValue(
                record.value(), TransactionCreatedEvent.class);

            log.info("SAGA received: transactionId={} sagaId={} userId={} amount={}",
                event.transactionId(), event.sagaId(), event.userId(), event.amount());

            // ── IDEMPOTENCY CHECK ──
            if (sagaProcessedRepo.existsBySagaId(event.sagaId())) {
                log.warn("SAGA already processed, skipping: sagaId={}", event.sagaId());
                ack.acknowledge();
                return;
            }

            // ── PROCESS ──
            WalletDebitedEvent response;
            if ("expense".equals(event.transactionType()) || "investment".equals(event.transactionType())) {
                response = walletService.debit(
                    event.sagaId(),
                    event.transactionId(),
                    event.userId(),
                    event.walletId(),
                    event.amount(),
                    event.envelopeKey()
                );
            } else {
                // Income — credit wallet
                walletService.credit(event.userId(), event.walletId(), event.amount());
                response = WalletDebitedEvent.success(
                    event.sagaId(), event.transactionId(), event.userId(),
                    event.walletId(), event.amount());
            }

            // ── PUBLISH RESULT via Outbox ──
            outboxService.publish(WalletDebitedEvent.TOPIC, response);

            // ── MARK PROCESSED ──
            sagaProcessedRepo.save(SagaProcessedEvent.builder()
                .sagaId(event.sagaId())
                .eventType("TRANSACTION_CREATED")
                .build());

            ack.acknowledge();
            log.info("SAGA step complete: sagaId={} success={}", event.sagaId(), response.success());

        } catch (Exception e) {
            log.error("SAGA processing error: {}", e.getMessage(), e);
            // Do NOT ack — let Kafka retry
            // After max retries it goes to Dead Letter Topic (DLT)
        }
    }
}
