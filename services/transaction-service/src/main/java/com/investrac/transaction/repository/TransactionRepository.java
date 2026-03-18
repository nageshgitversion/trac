package com.investrac.transaction.repository;

import com.investrac.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ── Basic lookups ──
    Optional<Transaction> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    Optional<Transaction> findBySagaId(String sagaId);

    // ── Paged filtered list ──
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.userId = :userId
          AND t.deleted = false
          AND (:type   IS NULL OR t.type     = :type)
          AND (:category IS NULL OR t.category = :category)
          AND (:from   IS NULL OR t.txDate  >= :from)
          AND (:to     IS NULL OR t.txDate  <= :to)
          AND (:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY t.txDate DESC, t.createdAt DESC
        """)
    Page<Transaction> findFiltered(
        @Param("userId")   Long userId,
        @Param("type")     Transaction.TransactionType type,
        @Param("category") String category,
        @Param("from")     LocalDate from,
        @Param("to")       LocalDate to,
        @Param("search")   String search,
        Pageable pageable
    );

    // ── Monthly summary totals ──
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
        WHERE t.userId = :userId
          AND t.deleted = false
          AND t.status  = 'COMPLETED'
          AND t.type    = :type
          AND YEAR(t.txDate)  = :year
          AND MONTH(t.txDate) = :month
        """)
    BigDecimal sumByTypeAndMonth(
        @Param("userId") Long userId,
        @Param("type")   Transaction.TransactionType type,
        @Param("year")   int year,
        @Param("month")  int month
    );

    // ── Category breakdown for a month ──
    @Query("""
        SELECT t.category, SUM(t.amount) FROM Transaction t
        WHERE t.userId = :userId
          AND t.deleted = false
          AND t.status  = 'COMPLETED'
          AND t.type    = 'EXPENSE'
          AND YEAR(t.txDate)  = :year
          AND MONTH(t.txDate) = :month
        GROUP BY t.category
        ORDER BY SUM(t.amount) DESC
        """)
    List<Object[]> categoryBreakdown(
        @Param("userId") Long userId,
        @Param("year")   int year,
        @Param("month")  int month
    );

    // ── Last N transactions (for home screen) ──
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.userId = :userId AND t.deleted = false AND t.status = 'COMPLETED'
        ORDER BY t.txDate DESC, t.createdAt DESC
        """)
    List<Transaction> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    // ── Wallet-linked transactions for a month (for wallet history) ──
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.userId   = :userId
          AND t.walletId = :walletId
          AND t.deleted  = false
          AND t.status   = 'COMPLETED'
          AND YEAR(t.txDate)  = :year
          AND MONTH(t.txDate) = :month
        ORDER BY t.txDate DESC
        """)
    List<Transaction> findByWalletAndMonth(
        @Param("userId")   Long userId,
        @Param("walletId") Long walletId,
        @Param("year")     int year,
        @Param("month")    int month
    );

    // ── Status update (used by SAGA consumer) ──
    @Modifying
    @Query("UPDATE Transaction t SET t.status = :status WHERE t.sagaId = :sagaId")
    int updateStatusBySagaId(
        @Param("sagaId") String sagaId,
        @Param("status") Transaction.TransactionStatus status
    );

    @Modifying
    @Query("UPDATE Transaction t SET t.status = :status, t.failureReason = :reason WHERE t.sagaId = :sagaId")
    int updateStatusAndReasonBySagaId(
        @Param("sagaId") String sagaId,
        @Param("status") Transaction.TransactionStatus status,
        @Param("reason") String reason
    );
}
