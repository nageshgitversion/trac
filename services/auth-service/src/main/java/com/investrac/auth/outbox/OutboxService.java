package com.investrac.auth.outbox;

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
 * Outbox Pattern implementation.
 *
 * Instead of publishing directly to Kafka (which can fail after DB commit),
 * we save the event to the outbox table in the SAME DB transaction.
 * A scheduler then picks up pending events and publishes them to Kafka.
 *
 * This guarantees: if the event is in DB → it WILL be published to Kafka.
 * No lost events even if Kafka is down or service crashes.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Save event to outbox in the current transaction.
     * Call this WITHIN a @Transactional method — same commit as business data.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(String topic, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxRepository.save(OutboxEvent.builder()
                .topic(topic)
                .payload(payload)
                .build());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event for topic {}: {}", topic, e.getMessage());
            throw new RuntimeException("Event serialization failed", e);
        }
    }

    /**
     * Scheduler runs every 5 seconds — picks up PENDING events and publishes to Kafka.
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository
            .findByStatusAndRetryCountLessThan(OutboxEvent.OutboxStatus.PENDING, 3);

        if (pendingEvents.isEmpty()) return;

        log.debug("Publishing {} outbox events", pendingEvents.size());

        pendingEvents.forEach(event -> {
            try {
                kafkaTemplate.send(event.getTopic(), event.getPayload()).get();
                event.setStatus(OutboxEvent.OutboxStatus.PUBLISHED);
                event.setPublishedAt(Instant.now());
                outboxRepository.save(event);
                log.debug("Published outbox event id={} to topic={}", event.getId(), event.getTopic());
            } catch (Exception e) {
                log.error("Failed to publish outbox event id={}: {}", event.getId(), e.getMessage());
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());
                if (event.getRetryCount() >= 3) {
                    event.setStatus(OutboxEvent.OutboxStatus.FAILED);
                    log.error("Outbox event id={} marked as FAILED after 3 retries", event.getId());
                }
                outboxRepository.save(event);
            }
        });
    }
}
