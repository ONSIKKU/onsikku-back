package com.onsikku.onsikku_back.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync   // 비동기 기능 활성화
public class AsyncConfig {

    /**
     * 푸시 알림 전용 스레드 풀
     * FCM 전송은 네트워크 I/O 작업이므로 코어 수보다 훨씬 많은 스레드를 두어도 괜찮습니다.
     */
    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);        // 기본 10개 스레드 유지
        executor.setMaxPoolSize(30);         // 피크 시 30개까지 확장
        executor.setQueueCapacity(1000);     // 전체 가족 알림을 대비해 큐를 넉넉히 잡음
        executor.setThreadNamePrefix("Push-Task-");
        executor.setWaitForTasksToCompleteOnShutdown(true); // 종료 시 작업 완료 대기
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * AI 분석 전용 스레드 풀
     * 전체 가족 데이터를 분석한다면 처리 시간이 길기 때문에 알림과는 별도로 분리하는 것이 좋습니다.
     */
    @Bean(name = "aiTaskExecutor")
    public Executor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // AI 서버가 분당 60개(초당 1개) 처리하므로, 동시 요청 스레드를 1~2개로 제한
        executor.setCorePoolSize(1);          // 상시 스레드 1개
        executor.setMaxPoolSize(2);           // 최대 2개까지만 허용 (안전빵)
        executor.setQueueCapacity(2000);      // 대신 대기 큐를 아주 넉넉하게 잡음 (가족들 리스트 보관용)

        // 큐가 가득 찼을 때 예외를 던지지 않고 큐가 빌 때까지 기다리도록 설정
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("AI-Slow-Task-");
        executor.initialize();
        return executor;
    }
}