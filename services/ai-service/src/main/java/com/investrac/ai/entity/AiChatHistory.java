package com.investrac.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "ai_chat_history", indexes = {
    @Index(name = "idx_chat_user",    columnList = "user_id"),
    @Index(name = "idx_chat_created", columnList = "user_id, created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AiChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Role role;

    /**
     * Message content.
     * SECURITY: never log this field — may contain user's financial details.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum Role { USER, ASSISTANT }
}
