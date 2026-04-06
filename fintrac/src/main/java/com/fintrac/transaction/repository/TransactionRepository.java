package com.fintrac.transaction.repository;

import com.fintrac.transaction.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserIdAndActiveTrue(Long id, Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.active = true " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:category IS NULL OR t.category = :category) " +
           "AND (:from IS NULL OR t.txDate >= :from) " +
           "AND (:to IS NULL OR t.txDate <= :to)")
    Page<Transaction> findFiltered(@Param("userId") Long userId,
                                   @Param("type") TransactionType type,
                                   @Param("category") TransactionCategory category,
                                   @Param("from") LocalDate from,
                                   @Param("to") LocalDate to,
                                   Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.userId = :userId AND t.type = :type AND t.active = true " +
           "AND YEAR(t.txDate) = :year AND MONTH(t.txDate) = :month")
    BigDecimal sumByTypeAndMonth(@Param("userId") Long userId,
                                 @Param("type") TransactionType type,
                                 @Param("year") int year,
                                 @Param("month") int month);
}
