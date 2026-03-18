package com.investrac.auth.repository;

import com.investrac.auth.entity.OtpVerification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(
        String email,
        OtpVerification.OtpPurpose purpose
    );

    @Modifying
    @Transactional
    @Query("UPDATE OtpVerification o SET o.attempts = o.attempts + 1 WHERE o.id = :id")
    void incrementAttempts(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE OtpVerification o SET o.used = true WHERE o.id = :id")
    void markUsed(@Param("id") Long id);

    // Cleanup old OTPs
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpVerification o WHERE o.expiresAt < :now OR o.used = true")
    void deleteExpiredAndUsed(@Param("now") LocalDateTime now);
}
