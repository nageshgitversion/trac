package com.investrac.transaction.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Outbox Pattern — guarantees every Kafka event is published
 * even if the service crashes after DB commit.
 *
 * Flow:
 *  1. Business code calls publish() INSIDE a @Transactional method
 *  2. Event is saved to outbox_events table (same transaction as business data)
 *  3. Scheduler polls every 5 seconds, publishes PENDING events to Kafka
 *  4. Marks published events as PUBLISHED
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionOutboxService {

    private final OutboxEventRepository        outboxRepository;
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final ObjectMapper                 objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(String topic, Object event) {
        try {
            outboxRepository.save(OutboxEvent.builder()
                .topic(topic)
                .payload(objectMapper.writeValueAsString(event))
                .build());
            log.debug("Outbox event queued for topic: {}", topic);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event for topic {}: {}", topic, e.getMessage());
            throw new RuntimeException("Event serialization failed", e);
        }
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxRepository
            .findByStatusAndRetryCountLessThan(OutboxEvent.Status.PENDING, 3);

        if (pending.isEmpty()) return;

        log.debug("Publishing {} outbox events to Kafka", pending.size());

        pending.forEach(event -> {
            try {
                kafkaTemplate.send(event.getTopic(), event.getPayload()).get();
                event.setStatus(OutboxEvent.Status.PUBLISHED);
                event.setPublishedAt(Instant.now());
            } catch (Exception e) {
                log.error("Outbox publish failed id={} topic={}: {}",
                    event.getId(), event.getTopic(), e.getMessage());
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());
                if (event.getRetryCount() >= 3) {
                    event.setStatus(OutboxEvent.Status.FAILED);
                    log.error("Outbox event id={} FAILED after 3 retries — check Dead Letter Topic",
                        event.getId());
                }
            }
            outboxRepository.save(event);
        });
    }
}
