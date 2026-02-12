package com.onsikku.onsikku_back.global.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${ai.server.base-url}")
    private String aiServerBaseUrl;

    @Bean
    public WebClient aiWebClient() {
        ConnectionProvider provider = ConnectionProvider.builder("ai-limiter")
            .maxConnections(5)    // 동시 연결을 5개 이하로 제한
            .pendingAcquireTimeout(Duration.ofSeconds(60)) // 커넥션 기다리는 시간 넉넉히
            .build();
        // 2. HttpClient 설정 (타임아웃 핵심)
        HttpClient httpClient = HttpClient.create(provider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 연결 타임아웃 5초
            .responseTimeout(Duration.ofSeconds(15))            // 응답 타임아웃 15초 (AI 5초 고려)
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(15, TimeUnit.SECONDS))
                .addHandlerLast(new WriteTimeoutHandler(15, TimeUnit.SECONDS)));

        return WebClient.builder()
            .baseUrl(aiServerBaseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient)) // 설정 적용
            .defaultHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}