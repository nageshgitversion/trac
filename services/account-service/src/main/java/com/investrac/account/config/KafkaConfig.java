package com.investrac.account.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka configuration for account-service.
 *
 * Account service is a PRODUCER only (publishes EMI-due events via outbox).
 * No Kafka consumers in this service.
 *
 * Dead Letter Topic configured for any future consumer additions.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        // Retry 3 times with 1s gap → then send to {topic}.DLT
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, ex) -> new TopicPartition(record.topic() + ".DLT", -1)
        );
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
    }
}
