package com.onsikku.onsikku_back.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onsikku.onsikku_back.domain.ai.dto.request.AiQuestionRequest;
import com.onsikku.onsikku_back.domain.ai.dto.response.AiQuestionResponse;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;


@Service
@Slf4j
@RequiredArgsConstructor
public class AiRequestService {

  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  /**
   * 답변 이후 AI 서버에 개인 파생 질문 생성을 요청하고, 응답을 DTO 객체로 반환합니다.
   */
  @Async("aiTaskExecutor")    // AsyncConfig에 정의된 aiTaskExecutor 사용
  @Transactional
  public AiQuestionResponse requestPersonalQuestionGeneratation(AiQuestionRequest requestDto) {
   try {
      com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
      String jsonBody = objectMapper.writeValueAsString(requestDto);
      log.info("AI 서버에 질문 생성을 요청합니다. JSON Body: {}", jsonBody);
    } catch (Exception e) {
      log.warn("DTO를 JSON으로 변환하는 데 실패했습니다.", e);
    }
    MediaType jsonUtf8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    try {
      return restClient.post()
          .uri("/api/v1/questions/generate/personal")
          .contentType(jsonUtf8)
          .body(objectMapper.writeValueAsString(requestDto))
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

  /**
   * 주에 한번 AI 서버에 가족 파생 질문 생성을 요청하고, 응답을 DTO 객체로 반환합니다.
   */
  @Async("aiTaskExecutor")    // AsyncConfig에 정의된 aiTaskExecutor 사용
  @Transactional
  public AiQuestionResponse requestFamilyQuestionFromBaseQuestion(AiQuestionRequest requestDto) {
    try {
      com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
      String jsonBody = objectMapper.writeValueAsString(requestDto);
      log.info("AI 서버에 질문 생성을 요청합니다. JSON Body: {}", jsonBody);
    } catch (Exception e) {
      log.warn("DTO를 JSON으로 변환하는 데 실패했습니다.", e);
    }
    MediaType jsonUtf8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    try {
      return restClient.post()
          .uri("/api/v1/questions/generate/family")
          .contentType(jsonUtf8)
          .body(objectMapper.writeValueAsString(requestDto))
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

  /**
   * 주에 한번 AI 서버에 가족 파생 질문 생성을 요청하고, 응답을 DTO 객체로 반환합니다.
   */
  @Async("aiTaskExecutor")    // AsyncConfig에 정의된 aiTaskExecutor 사용
  @Transactional
  public AiQuestionResponse requestFamilyQuestionFromRecentQuestions(AiQuestionRequest requestDto) {
    try {
      com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
      String jsonBody = objectMapper.writeValueAsString(requestDto);
      log.info("AI 서버에 질문 생성을 요청합니다. JSON Body: {}", jsonBody);
    } catch (Exception e) {
      log.warn("DTO를 JSON으로 변환하는 데 실패했습니다.", e);
    }
    MediaType jsonUtf8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    try {
      return restClient.post()
          .uri("/api/v1/questions/generate/family-recent")
          .contentType(jsonUtf8)
          .body(objectMapper.writeValueAsString(requestDto))
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