package com.investrac.wallet.repository;

import com.investrac.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserIdAndMonth(Long userId, String month);

    Optional<Wallet> findByUserIdAndActiveTrue(Long userId);

    Optional<Wallet> findByUserIdAndMonthAndActiveTrue(Long userId, String month);

    boolean existsByUserIdAndMonth(Long userId, String month);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance - :amount WHERE w.id = :id AND w.balance >= :amount")
    int debitBalance(@Param("id") Long walletId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance + :amount WHERE w.id = :id")
    void creditBalance(@Param("id") Long walletId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE Wallet w SET w.topup = w.topup + :amount, w.balance = w.balance + :amount WHERE w.id = :id")
    void topUp(@Param("id") Long walletId, @Param("amount") BigDecimal amount);
}
