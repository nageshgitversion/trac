package com.investrac.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investrac.notification.entity.Notification;
import com.investrac.notification.entity.NotificationPreference;
import com.investrac.notification.repository.NotificationPreferenceRepository;
import com.investrac.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock NotificationRepository           notificationRepository;
    @Mock NotificationPreferenceRepository preferenceRepository;
    @Mock FcmPushService                   fcmPushService;
    @Mock EmailService                     emailService;
    @Mock ObjectMapper                     objectMapper;

    @InjectMocks
    NotificationService notificationService;

    private NotificationPreference defaultPrefs;

    @BeforeEach
    void setUp() {
        defaultPrefs = NotificationPreference.builder()
            .userId(100L)
            .pushEnabled(true)
            .emailEnabled(true)
            .fcmToken("valid-fcm-token")
            .email("arjun@investrac.in")
            .transactionNotif(true)
            .emiNotif(true)
            .walletAlertNotif(true)
            .portfolioNotif(false)   // off by default
            .aiInsightNotif(true)
            .build();
    }

    @Test
    @DisplayName("send: saves notification to DB, sends FCM push")
    void send_SavesToDB_SendsFcm() {
        when(preferenceRepository.findByUserId(100L)).thenReturn(Optional.of(defaultPrefs));
        Notification saved = Notification.builder()
            .id(1L).userId(100L).title("T").body("B")
            .type(Notification.NotificationType.TRANSACTION_COMPLETED)
            .channel(Notification.NotificationChannel.PUSH)
            .build();
        when(notificationRepository.save(any())).thenReturn(saved);
        when(fcmPushService.sendPush(any(), any(), any(), any())).thenReturn(true);

        notificationService.send(100L, "T", "B",
            Notification.NotificationType.TRANSACTION_COMPLETED, Map.of());

        // DB save called twice: initial save + update with sent status
        verify(notificationRepository, times(2)).save(any());
        verify(fcmPushService).sendPush(eq("valid-fcm-token"), eq("T"), eq("B"), any());
    }

    @Test
    @DisplayName("send: suppressed when type is disabled in preferences")
    void send_TypeDisabled_NoFcmCall() {
        when(preferenceRepository.findByUserId(100L)).thenReturn(Optional.of(defaultPrefs));
        Notification saved = Notification.builder()
            .id(1L).userId(100L).title("T").body("B")
            .type(Notification.NotificationType.PORTFOLIO_SYNCED)
            .channel(Notification.NotificationChannel.PUSH)
            .build();
        when(notificationRepository.save(any())).thenReturn(saved);

        // portfolioNotif = false
        notificationService.send(100L, "T", "B",
            Notification.NotificationType.PORTFOLIO_SYNCED, Map.of());

        // Notification saved to DB (audit trail always)
        verify(notificationRepository).save(any());
        // But FCM push NOT sent
        verify(fcmPushService, never()).sendPush(any(), any(), any(), any());
    }

    @Test
    @DisplayName("send: clears stale FCM token when push fails with UNREGISTERED")
    void send_StaleToken_Cleared() {
        when(preferenceRepository.findByUserId(100L)).thenReturn(Optional.of(defaultPrefs));
        Notification saved = Notification.builder()
            .id(1L).userId(100L).title("T").body("B")
            .type(Notification.NotificationType.TRANSACTION_COMPLETED)
            .channel(Notification.NotificationChannel.PUSH)
            .build();
        when(notificationRepository.save(any())).thenReturn(saved);
        // FCM push fails
        when(fcmPushService.sendPush(any(), any(), any(), any())).thenReturn(false);

        notificationService.send(100L, "T", "B",
            Notification.NotificationType.TRANSACTION_COMPLETED, Map.of());

        // Preferences saved with cleared token
        ArgumentCaptor<NotificationPreference> prefCaptor =
            ArgumentCaptor.forClass(NotificationPreference.class);
        verify(preferenceRepository).save(prefCaptor.capture());
        assertThat(prefCaptor.getValue().getFcmToken()).isNull();
    }

    @Test
    @DisplayName("send: creates preferences with defaults when user has no preferences")
    void send_NoPreferences_CreatesDefaults() {
        when(preferenceRepository.findByUserId(100L)).thenReturn(Optional.empty());
        NotificationPreference newPrefs = NotificationPreference.builder()
            .userId(100L).pushEnabled(true).build();
        when(preferenceRepository.save(any(NotificationPreference.class)))
            .thenReturn(newPrefs);
        Notification saved = Notification.builder()
            .id(1L).userId(100L).title("T").body("B")
            .type(Notification.NotificationType.WELCOME)
            .channel(Notification.NotificationChannel.PUSH)
            .build();
        when(notificationRepository.save(any())).thenReturn(saved);

        notificationService.send(100L, "T", "B",
            Notification.NotificationType.WELCOME, Map.of());

        // Default preferences created
        verify(preferenceRepository).save(argThat(p ->
            p.getUserId() == 100L && p.isPushEnabled()));
    }

    @Test
    @DisplayName("markRead: returns true when notification exists and belongs to user")
    void markRead_ExistingNotification_ReturnsTrue() {
        when(notificationRepository.markRead(1L, 100L)).thenReturn(1);
        assertThat(notificationService.markRead(1L, 100L)).isTrue();
    }

    @Test
    @DisplayName("markRead: returns false when notification not found for user")
    void markRead_NotFound_ReturnsFalse() {
        when(notificationRepository.markRead(999L, 100L)).thenReturn(0);
        assertThat(notificationService.markRead(999L, 100L)).isFalse();
    }

    @Test
    @DisplayName("updatePreferences: updates FCM token and per-type toggles")
    void updatePreferences_UpdatesCorrectly() {
        when(preferenceRepository.findByUserId(100L)).thenReturn(Optional.of(defaultPrefs));
        when(preferenceRepository.save(any())).thenReturn(defaultPrefs);

        notificationService.updatePreferences(100L,
            null, null, "new-fcm-token", null,
            null, null, null, true, null);

        ArgumentCaptor<NotificationPreference> captor =
            ArgumentCaptor.forClass(NotificationPreference.class);
        verify(preferenceRepository).save(captor.capture());

        assertThat(captor.getValue().getFcmToken()).isEqualTo("new-fcm-token");
        assertThat(captor.getValue().isPortfolioNotif()).isTrue();  // turned on
    }

    @Test
    @DisplayName("getUnreadCount: returns correct count from repository")
    void getUnreadCount_ReturnsCount() {
        when(notificationRepository.countByUserIdAndReadFalse(100L)).thenReturn(5L);
        assertThat(notificationService.getUnreadCount(100L)).isEqualTo(5L);
    }
}
