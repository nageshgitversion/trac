package com.investrac.auth.repository;

import com.investrac.auth.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmailAndActiveTrue(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.loginAttempts = u.loginAttempts + 1 WHERE u.id = :userId")
    void incrementLoginAttempts(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void lockUserUntil(@Param("userId") Long userId, @Param("lockedUntil") LocalDateTime lockedUntil);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.loginAttempts = 0, u.lockedUntil = null WHERE u.id = :userId")
    void resetLoginAttempts(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :userId")
    void markEmailVerified(@Param("userId") Long userId);
}
