package com.investrac.ai.repository;

import com.investrac.ai.entity.AiChatHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AiChatHistoryRepository extends JpaRepository<AiChatHistory, Long> {

    // Last N messages for a user (for context window)
    @Query("""
        SELECT h FROM AiChatHistory h
        WHERE h.userId = :userId
        ORDER BY h.createdAt DESC
        """)
    List<AiChatHistory> findRecentByUserId(
        @Param("userId") Long userId,
        Pageable pageable
    );

    // All messages in chronological order (for UI display)
    List<AiChatHistory> findByUserIdOrderByCreatedAtAsc(Long userId);

    // Cleanup old chat history (keep last 30 days)
    @Modifying
    @Query("DELETE FROM AiChatHistory h WHERE h.userId = :userId AND h.createdAt < :before")
    void deleteOldHistory(
        @Param("userId") Long userId,
        @Param("before") Instant before
    );
}
