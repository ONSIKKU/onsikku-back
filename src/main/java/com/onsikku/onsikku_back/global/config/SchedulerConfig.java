
package com.onsikku.onsikku_back.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling     // 스케줄링 기능 활성화
public class SchedulerConfig {
    // 필요 시 여기에 Scheduling 전용 Task Executor를 정의할 수 있습니다.
    // (현재는 기본 스케줄러 풀을 사용하게 됩니다.)
}