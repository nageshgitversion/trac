package com.investrac.wallet.outbox;

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

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletOutboxService {

    private final WalletOutboxRepository       outboxRepository;
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final ObjectMapper                 objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(String topic, Object event) {
        try {
            outboxRepository.save(WalletOutboxEvent.builder()
                .topic(topic)
                .payload(objectMapper.writeValueAsString(event))
                .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Outbox serialization failed", e);
        }
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPending() {
        List<WalletOutboxEvent> pending = outboxRepository
            .findByStatusAndRetryCountLessThan(WalletOutboxEvent.Status.PENDING, 3);
        pending.forEach(event -> {
            try {
                kafkaTemplate.send(event.getTopic(), event.getPayload()).get();
                event.setStatus(WalletOutboxEvent.Status.PUBLISHED);
                event.setPublishedAt(Instant.now());
            } catch (Exception e) {
                log.error("Outbox publish failed id={}: {}", event.getId(), e.getMessage());
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());
                if (event.getRetryCount() >= 3) event.setStatus(WalletOutboxEvent.Status.FAILED);
            }
            outboxRepository.save(event);
        });
    }
}
