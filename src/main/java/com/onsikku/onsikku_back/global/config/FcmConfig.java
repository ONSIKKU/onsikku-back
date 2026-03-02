package com.onsikku.onsikku_back.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;

@Configuration
@Slf4j
@RequiredArgsConstructor // 생성자 주입
public class FcmConfig {

    @Value("${fcm.config.path}")
    private String firebaseConfigPath;

    // 스프링 리소스 로더 주입
    private final ResourceLoader resourceLoader;

    @PostConstruct
    public void initialize() {
        try {
            // ResourceLoader는 'classpath:', 'file:' 이해 가능
            Resource resource = resourceLoader.getResource(firebaseConfigPath);

            // 파일이 진짜 있는지 체크
            if (!resource.exists()) {
                log.error("Firebase 키 파일을 찾을 수 없습니다: {}", firebaseConfigPath);
                return;
            }

            log.info("[FCM] firebaseConfigPath={}", firebaseConfigPath);
            log.info("[FCM] resource={}", resource);
            log.info("[FCM] resource URL={}", resource.getURL());

            // 스트림으로 읽기
            try (InputStream inputStream = resource.getInputStream()) {
                GoogleCredentials creds = GoogleCredentials.fromStream(inputStream);

                if (creds instanceof ServiceAccountCredentials sac) {
                    log.info("[FCM] using client_email={}", sac.getClientEmail());
                    log.info("[FCM] using project_id={}", sac.getProjectId());
                } else {
                    log.info("[FCM] credentials type={}", creds.getClass().getName());
                }

                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .build();
                FirebaseApp.initializeApp(options);
                log.info("FirebaseApp 초기화 완료! (경로: {})", firebaseConfigPath);
            }

        } catch (Exception e) {
            log.error("FirebaseApp 초기화 실패: {}", e.getMessage());
        }
    }
}