package com.onsikku.onsikku_back.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync   // 비동기 기능 활성화
public class AsyncConfig {

    /**
     * 푸시 알림 전용 스레드 풀 정의
     */
    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);        // 코어 스레드 수: 기본적으로 CPU 코어 수에 맞추거나, I/O 작업이 많으면 그 이상 설정
        executor.setMaxPoolSize(3);         // 최대 스레드 수
        executor.setQueueCapacity(100);     // 큐 용량: 큐에 쌓일 수 있는 대기 작업의 수
        executor.setThreadNamePrefix("Notification-Task-"); // 스레드 이름 접두사 설정
        executor.initialize();
        return executor;
    }

    // AI 분석 전용 Executor (지연 시간 활용)
    @Bean(name = "aiTaskExecutor")
    public Executor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);        // 1코어라도 2개 이상 설정하여 I/O 효율을 높임
        executor.setMaxPoolSize(4);         // 4개까지만 동시 AI 요청 허용
        executor.setQueueCapacity(500);     // 큐에 대기할 수 있는 작업 수ㅌ 확보
        executor.setThreadNamePrefix("AI-Analysis-Task-");
        executor.initialize();
        return executor;
    }
}