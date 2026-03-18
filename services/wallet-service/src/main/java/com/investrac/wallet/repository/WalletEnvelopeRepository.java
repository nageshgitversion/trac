package com.investrac.wallet.repository;

import com.investrac.wallet.entity.WalletEnvelope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletEnvelopeRepository extends JpaRepository<WalletEnvelope, Long> {

    List<WalletEnvelope> findByWalletId(Long walletId);

    Optional<WalletEnvelope> findByWalletIdAndEnvelopeKey(Long walletId, String envelopeKey);

    @Modifying
    @Query("UPDATE WalletEnvelope e SET e.spent = e.spent + :amount WHERE e.wallet.id = :walletId AND e.envelopeKey = :key")
    int addSpending(@Param("walletId") Long walletId, @Param("key") String key, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE WalletEnvelope e SET e.spent = GREATEST(e.spent - :amount, 0) WHERE e.wallet.id = :walletId AND e.envelopeKey = :key")
    void reverseSpending(@Param("walletId") Long walletId, @Param("key") String key, @Param("amount") BigDecimal amount);
}
