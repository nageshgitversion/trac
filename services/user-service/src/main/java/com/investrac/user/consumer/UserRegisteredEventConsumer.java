package com.investrac.user.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investrac.common.events.UserRegisteredEvent;
import com.investrac.user.dto.request.CreateProfileRequest;
import com.investrac.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens to investrac.user.registered published by auth-service.
 * Creates a UserProfile in user-service DB automatically.
 *
 * This decouples auth-service from user-service:
 *  - auth-service owns identity (email/password/tokens)
 *  - user-service owns profile (name/DOB/KYC/preferences)
 *
 * Idempotent — createProfile() checks for existing profile first.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserRegisteredEventConsumer {

    private final UserService  userService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics    = UserRegisteredEvent.TOPIC,
        groupId   = "${spring.kafka.consumer.group-id:user-service-group}",
        concurrency = "1"
    )
    @Transactional
    public void onUserRegistered(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            UserRegisteredEvent event = objectMapper.readValue(
                record.value(), UserRegisteredEvent.class);

            log.info("Creating user profile for userId={} email={}",
                event.userId(), event.email());

            CreateProfileRequest req = new CreateProfileRequest();
            req.setUserId(event.userId());
            req.setName(event.name());
            req.setEmail(event.email());

            userService.createProfile(req);
            log.info("User profile created successfully for userId={}", event.userId());

        } catch (Exception e) {
            log.error("Error creating user profile from registration event: {}", e.getMessage(), e);
            // Don't rethrow — allow retry via DLT
        } finally {
            ack.acknowledge();
        }
    }
}
