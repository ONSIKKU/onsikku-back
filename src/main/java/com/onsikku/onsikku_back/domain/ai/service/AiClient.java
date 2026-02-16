package com.onsikku.onsikku_back.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onsikku.onsikku_back.domain.ai.dto.request.AiQuestionRequest;
import com.onsikku.onsikku_back.domain.ai.dto.response.AiQuestionResponse;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    // 공통 POST 요청 메서드
    public AiQuestionResponse postForAiRequest(String uri, AiQuestionRequest requestDto) {
        try {
            log.info("AI 서버 요청 [{}]: {}", uri, objectMapper.writeValueAsString(requestDto));
            //MediaType jsonUtf8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);
            return restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto) // RestClient가 내부적으로 ObjectMapper를 사용하므로 직접 전달 가능
                .retrieve()
                .body(AiQuestionResponse.class);
        } catch (HttpClientErrorException e) {
            log.error("AI 서버 요청 중 클라이언트 오류가 발생했습니다. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BaseException(BaseResponseStatus.AI_SERVER_COMMUNICATION_ERROR);
        } catch (Exception e) {
            log.error("AI 서버 요청 중 알 수 없는 오류가 발생했습니다.", e);
            throw new BaseException(BaseResponseStatus.AI_SERVER_COMMUNICATION_ERROR);
        }
    }
}