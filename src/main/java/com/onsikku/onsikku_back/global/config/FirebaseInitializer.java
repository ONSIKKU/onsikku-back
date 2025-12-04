package com.onsikku.onsikku_back.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class FirebaseInitializer {

    @Value("${fcm.credentials-json}")
    private String fcmCredentialsJson; 

    @PostConstruct
    public void initialize() {
        if (fcmCredentialsJson == null || fcmCredentialsJson.isBlank()) {
            log.warn("FCM Credentials JSON이 설정되지 않았습니다. Firebase SDK 초기화를 건너뜝니다.");
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            try {
                ByteArrayInputStream serviceAccount = new ByteArrayInputStream(fcmCredentialsJson.getBytes(StandardCharsets.UTF_8));
                // Credentials 로드
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                // 옵션 구성
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
                // 앱 초기화
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK 초기화 완료.");
            } catch (IOException e) {
                log.error("Firebase Admin SDK 초기화 실패: JSON 인증 정보를 로드할 수 없습니다.", e);
            }
        }
    }
}