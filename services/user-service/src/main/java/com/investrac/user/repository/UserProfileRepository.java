package com.investrac.user.repository;

import com.investrac.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);

    Optional<UserProfile> findByEmail(String email);

    boolean existsByUserId(Long userId);

    // Internal: used by auth-service event consumer to create profile on registration
    @Modifying
    @Query("UPDATE UserProfile u SET u.kycVerified = true WHERE u.userId = :userId")
    void markKycVerified(@Param("userId") Long userId);

    // Update just the name (used after auth-service profile sync)
    @Modifying
    @Query("UPDATE UserProfile u SET u.name = :name, u.phone = :phone WHERE u.userId = :userId")
    int updateNameAndPhone(
        @Param("userId") Long userId,
        @Param("name")   String name,
        @Param("phone")  String phone
    );
}
