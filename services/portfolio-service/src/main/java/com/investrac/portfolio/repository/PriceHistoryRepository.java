package com.investrac.portfolio.repository;

import com.investrac.portfolio.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    // ── Last N days price history for a holding ──
    @Query("""
        SELECT ph FROM PriceHistory ph
        WHERE ph.holdingId = :holdingId
          AND ph.recordedAt >= :from
        ORDER BY ph.recordedAt ASC
        """)
    List<PriceHistory> findByHoldingIdSince(
        @Param("holdingId") Long holdingId,
        @Param("from")      LocalDate from
    );

    // ── Latest price for a holding ──
    Optional<PriceHistory> findTopByHoldingIdOrderByRecordedAtDesc(Long holdingId);

    // ── Check if already recorded today (prevent duplicates) ──
    boolean existsByHoldingIdAndRecordedAt(Long holdingId, LocalDate recordedAt);

    // ── Portfolio value over time (all holdings for a user, aggregated) ──
    @Query("""
        SELECT ph.recordedAt, SUM(ph.price * h.units)
        FROM PriceHistory ph
        JOIN Holding h ON h.id = ph.holdingId
        WHERE h.userId = :userId
          AND h.active = true
          AND ph.recordedAt >= :from
        GROUP BY ph.recordedAt
        ORDER BY ph.recordedAt ASC
        """)
    List<Object[]> getPortfolioValueHistory(
        @Param("userId") Long userId,
        @Param("from")   LocalDate from
    );
}
