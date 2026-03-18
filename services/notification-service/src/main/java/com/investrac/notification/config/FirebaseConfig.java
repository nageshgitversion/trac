package com.investrac.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

/**
 * Firebase Admin SDK initialization.
 *
 * Credentials loaded from:
 *   - File path (production): FIREBASE_CREDENTIALS_PATH env var
 *   - Classpath (dev): firebase-service-account.json
 *
 * If FCM is disabled (spring.fcm.enabled=false), initialization is skipped.
 * This allows running the service in test environments without Firebase.
 *
 * SECURITY: Never commit firebase-service-account.json to Git.
 *           Add to .gitignore. In production, use Kubernetes Secret or
 *           AWS Secrets Manager.
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${spring.fcm.enabled:true}")
    private boolean fcmEnabled;

    @Value("${spring.fcm.credentials-path:firebase-service-account.json}")
    private String credentialsPath;

    @PostConstruct
    public void initFirebase() {
        if (!fcmEnabled) {
            log.info("FCM disabled — skipping Firebase initialization");
            return;
        }

        // Avoid re-initializing if already done (happens in test contexts)
        if (!FirebaseApp.getApps().isEmpty()) {
            log.debug("Firebase already initialized");
            return;
        }

        try {
            InputStream serviceAccount;

            // Try to load from classpath first (dev), then filesystem (prod)
            try {
                serviceAccount = new ClassPathResource(credentialsPath).getInputStream();
                log.info("Firebase credentials loaded from classpath: {}", credentialsPath);
            } catch (Exception e) {
                // Fall back to absolute path (production secret mount)
                serviceAccount = new java.io.FileInputStream(credentialsPath);
                log.info("Firebase credentials loaded from filesystem: {}", credentialsPath);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized successfully");

        } catch (Exception e) {
            log.error("Firebase initialization failed: {} — push notifications will not work",
                e.getMessage());
            // Do NOT throw — service should still work for email notifications
        }
    }
}
