package com.investrac.account.repository;

import com.investrac.account.entity.VirtualAccount;
import com.investrac.account.entity.VirtualAccount.AccountType;
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
public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Long> {

    // ── Basic lookups ──
    Optional<VirtualAccount> findByIdAndUserId(Long id, Long userId);

    List<VirtualAccount> findByUserIdAndActiveTrueOrderByCreatedAtDesc(Long userId);

    List<VirtualAccount> findByUserIdAndTypeAndActiveTrue(Long userId, AccountType type);

    // ── EMI Scheduler — find all accounts with EMI due today ──
    @Query("""
        SELECT a FROM VirtualAccount a
        WHERE a.emiDay = :day
          AND a.type IN ('LOAN', 'RD')
          AND a.active = true
          AND (a.maturityDate IS NULL OR a.maturityDate >= :today)
        """)
    List<VirtualAccount> findEmiDueAccounts(
        @Param("day")   int day,
        @Param("today") LocalDate today
    );

    // ── Maturity alert — accounts maturing within N days ──
    @Query("""
        SELECT a FROM VirtualAccount a
        WHERE a.type IN ('FD', 'RD')
          AND a.active = true
          AND a.maturityDate IS NOT NULL
          AND a.maturityDate BETWEEN :from AND :to
        """)
    List<VirtualAccount> findMaturingBetween(
        @Param("from") LocalDate from,
        @Param("to")   LocalDate to
    );

    // ── Portfolio summary totals per type ──
    @Query("""
        SELECT a.type, COALESCE(SUM(a.balance), 0)
        FROM VirtualAccount a
        WHERE a.userId = :userId AND a.active = true
        GROUP BY a.type
        """)
    List<Object[]> getSummaryByType(@Param("userId") Long userId);

    // ── Update balance ──
    @Modifying
    @Query("UPDATE VirtualAccount a SET a.balance = a.balance + :delta WHERE a.id = :id")
    void adjustBalance(@Param("id") Long id, @Param("delta") BigDecimal delta);

    // ── Soft delete ──
    @Modifying
    @Query("UPDATE VirtualAccount a SET a.active = false WHERE a.id = :id AND a.userId = :userId")
    int deactivate(@Param("id") Long id, @Param("userId") Long userId);

    // ── Goal progress check (for notification triggers) ──
    @Query("""
        SELECT a FROM VirtualAccount a
        WHERE a.userId = :userId
          AND a.type = 'SAVINGS'
          AND a.goalAmount IS NOT NULL
          AND a.goalAmount > 0
          AND a.active = true
        """)
    List<VirtualAccount> findGoalAccounts(@Param("userId") Long userId);

    // ── Total committed EMI for wallet calculation ──
    @Query("""
        SELECT COALESCE(SUM(a.emiAmount), 0)
        FROM VirtualAccount a
        WHERE a.userId = :userId
          AND a.type IN ('LOAN', 'RD')
          AND a.active = true
          AND (a.maturityDate IS NULL OR a.maturityDate >= :today)
        """)
    BigDecimal getTotalMonthlyEmi(
        @Param("userId") Long userId,
        @Param("today")  LocalDate today
    );
}
