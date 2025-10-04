package com.onsikku.onsikku_back.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Value("${ai.server.base-url}")
  private String aiServerBaseUrl;

  @Bean
  public RestClient restClient(RestClient.Builder builder) {
    return builder
        .baseUrl(aiServerBaseUrl)
        .build();
  }
}

