package com.investrac.portfolio.outbox;

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

@Service @Slf4j @RequiredArgsConstructor
public class PortfolioOutboxService {

    private final PortfolioOutboxRepository    outboxRepository;
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final ObjectMapper                 objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(String topic, Object event) {
        try {
            outboxRepository.save(PortfolioOutboxEvent.builder()
                .topic(topic).payload(objectMapper.writeValueAsString(event)).build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Outbox serialization failed", e);
        }
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPending() {
        List<PortfolioOutboxEvent> pending = outboxRepository
            .findByStatusAndRetryCountLessThan(PortfolioOutboxEvent.Status.PENDING, 3);
        pending.forEach(ev -> {
            try {
                kafkaTemplate.send(ev.getTopic(), ev.getPayload()).get();
                ev.setStatus(PortfolioOutboxEvent.Status.PUBLISHED);
                ev.setPublishedAt(Instant.now());
            } catch (Exception e) {
                log.error("Outbox publish failed id={}: {}", ev.getId(), e.getMessage());
                ev.setRetryCount(ev.getRetryCount() + 1);
                ev.setLastError(e.getMessage());
                if (ev.getRetryCount() >= 3) ev.setStatus(PortfolioOutboxEvent.Status.FAILED);
            }
            outboxRepository.save(ev);
        });
    }
}
