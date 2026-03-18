package com.investrac.notification.repository;

import com.investrac.notification.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository
    extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Modifying @Transactional
    @Query("UPDATE NotificationPreference p SET p.fcmToken = null WHERE p.fcmToken = :token")
    void clearFcmToken(@Param("token") String token);

    @Modifying @Transactional
    @Query("UPDATE NotificationPreference p SET p.fcmToken = :token WHERE p.userId = :userId")
    int updateFcmToken(@Param("userId") Long userId, @Param("token") String token);
}
