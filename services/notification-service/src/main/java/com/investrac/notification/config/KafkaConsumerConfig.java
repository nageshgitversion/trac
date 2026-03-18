package com.investrac.notification.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka error handling configuration.
 *
 * Notification service is a pure CONSUMER — only reads events, never produces
 * business events (does save to DB and sends push/email).
 *
 * Error handling:
 *   - Retry 3 times with 1-second gap
 *   - After 3 failures → publish to Dead Letter Topic ({topic}.DLT)
 *   - Ops team monitors DLT for missed notifications
 *
 * Notification failures are non-critical to financial data integrity
 * (transactions are already committed) but must be tracked.
 */
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, ex) -> new TopicPartition(record.topic() + ".DLT", -1)
        );
        // 3 retries, 1 second apart
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
    }
}
