package com.investrac.ai.repository;

import com.investrac.ai.entity.AiInsight;
import com.investrac.ai.entity.AiInsight.InsightType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AiInsightRepository extends JpaRepository<AiInsight, Long> {

    // Unread insights for a user — shown on home screen
    List<AiInsight> findByUserIdAndReadFalseOrderByPriorityAscGeneratedAtDesc(Long userId);

    // All insights (read + unread), paginated
    @Query("""
        SELECT i FROM AiInsight i
        WHERE i.userId = :userId
        ORDER BY i.generatedAt DESC
        """)
    List<AiInsight> findByUserIdOrderByGeneratedAtDesc(
        @Param("userId") Long userId,
        Pageable pageable
    );

    // Count unread — for notification badge
    long countByUserIdAndReadFalse(Long userId);

    // Find by id + userId (ownership check)
    Optional<AiInsight> findByIdAndUserId(Long id, Long userId);

    // Mark single insight as read
    @Modifying
    @Transactional
    @Query("UPDATE AiInsight i SET i.read = true WHERE i.id = :id AND i.userId = :userId")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    // Mark all insights read for a user
    @Modifying
    @Transactional
    @Query("UPDATE AiInsight i SET i.read = true WHERE i.userId = :userId AND i.read = false")
    void markAllRead(@Param("userId") Long userId);

    // Check if insight already generated today (prevent duplicates from scheduler)
    @Query("""
        SELECT COUNT(i) > 0 FROM AiInsight i
        WHERE i.userId = :userId
          AND i.generatedAt >= :since
        """)
    boolean hasInsightGeneratedSince(
        @Param("userId") Long userId,
        @Param("since")  Instant since
    );

    // Cleanup old read insights (keep last 90 days)
    @Modifying
    @Transactional
    @Query("DELETE FROM AiInsight i WHERE i.read = true AND i.generatedAt < :before")
    void deleteOldReadInsights(@Param("before") Instant before);
}
