package com.investrac.portfolio.repository;

import com.investrac.portfolio.entity.Holding;
import com.investrac.portfolio.entity.Holding.HoldingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    // ── Basic lookups ──
    Optional<Holding> findByIdAndUserId(Long id, Long userId);

    List<Holding> findByUserIdAndActiveTrueOrderByCurrentValueDesc(Long userId);

    List<Holding> findByUserIdAndTypeAndActiveTrue(Long userId, HoldingType type);

    // ── Scheduler: find all holdings that can be auto-updated ──
    @Query("""
        SELECT h FROM Holding h
        WHERE h.updatable = true
          AND h.active    = true
          AND h.symbol IS NOT NULL
          AND h.symbol  <> ''
        ORDER BY h.type, h.name
        """)
    List<Holding> findAllUpdatableHoldings();

    // ── Find by symbol across all users (batch price sync) ──
    @Query("""
        SELECT DISTINCT h.symbol FROM Holding h
        WHERE h.updatable = true AND h.active = true
          AND h.symbol IS NOT NULL
          AND h.type IN ('EQUITY_MF', 'DEBT_MF')
        """)
    List<String> findDistinctMfSymbols();

    @Query("""
        SELECT DISTINCT h.symbol FROM Holding h
        WHERE h.updatable = true AND h.active = true
          AND h.symbol IS NOT NULL
          AND h.type = 'STOCKS'
        """)
    List<String> findDistinctStockSymbols();

    // ── Portfolio totals ──
    @Query("""
        SELECT COALESCE(SUM(h.invested), 0)
        FROM Holding h
        WHERE h.userId = :userId AND h.active = true
        """)
    BigDecimal sumInvested(@Param("userId") Long userId);

    @Query("""
        SELECT COALESCE(SUM(h.currentValue), 0)
        FROM Holding h
        WHERE h.userId = :userId AND h.active = true
        """)
    BigDecimal sumCurrentValue(@Param("userId") Long userId);

    // ── Type breakdown for asset allocation chart ──
    @Query("""
        SELECT h.type, COALESCE(SUM(h.currentValue), 0)
        FROM Holding h
        WHERE h.userId = :userId AND h.active = true
        GROUP BY h.type
        """)
    List<Object[]> getValueByType(@Param("userId") Long userId);

    // ── Update price + value after sync ──
    @Modifying
    @Query("""
        UPDATE Holding h
        SET h.currentPrice = :price,
            h.currentValue = :value,
            h.lastSynced   = :syncedAt
        WHERE h.id = :id
        """)
    void updatePriceAndValue(
        @Param("id")       Long id,
        @Param("price")    BigDecimal price,
        @Param("value")    BigDecimal value,
        @Param("syncedAt") Instant syncedAt
    );

    // ── Soft delete ──
    @Modifying
    @Query("UPDATE Holding h SET h.active = false WHERE h.id = :id AND h.userId = :userId")
    int deactivate(@Param("id") Long id, @Param("userId") Long userId);
}
