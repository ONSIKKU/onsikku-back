package com.onsikku.onsikku_back.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onsikku.onsikku_back.domain.ai.domain.AnswerAnalysis;
import com.onsikku.onsikku_back.domain.ai.dto.request.AiQuestionRequest;
import com.onsikku.onsikku_back.domain.ai.dto.request.AnswerAnalysisRequest;
import com.onsikku.onsikku_back.domain.ai.dto.request.MemberAssignRequest;
import com.onsikku.onsikku_back.domain.ai.dto.request.MemberInfo;
import com.onsikku.onsikku_back.domain.ai.dto.response.AiQuestionResponse;
import com.onsikku.onsikku_back.domain.ai.dto.response.AnswerAnalysisResponse;
import com.onsikku.onsikku_back.domain.ai.dto.response.MemberAssignResponse;
import com.onsikku.onsikku_back.domain.ai.repository.AnswerAnalysisRepository;
import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiRequestService {

  private final RestClient restClient;
  private final AnswerAnalysisRepository answerAnalysisRepository;
  private final ObjectMapper objectMapper;
  private final WebClient webClient;

  /**
   * AI 서버에 질문 생성을 요청하고, 응답을 DTO 객체로 반환합니다.
   */
  public AiQuestionResponse requestQuestionGeneration(AiQuestionRequest requestDto) {
   /* try {
      com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
      String jsonBody = objectMapper.writeValueAsString(requestDto);
      log.info("AI 서버에 질문 생성을 요청합니다. JSON Body: {}", jsonBody);
    } catch (Exception e) {
      log.warn("DTO를 JSON으로 변환하는 데 실패했습니다.", e);
    }
    MediaType jsonUtf8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);
    */
    try {
      /*return restClient.post()
          .uri("/api/v1/questions/api")
          .contentType(jsonUtf8)
          .body(objectMapper.writeValueAsString(requestDto))
          .retrieve()
          .body(AiQuestionResponse.class);
          */
      return webClient.post()
          .uri("/api/v1/questions/api")
          .bodyValue(requestDto)
          .retrieve()
          // WebClient 에러 처리 (필수)
          .onStatus(status -> status.isError(), clientResponse -> {
            log.error("AI 서버 WebClient 오류 발생. Status: {}", clientResponse.statusCode());
            return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> {
                  log.error("Error Body: {}", errorBody);
                  return reactor.core.publisher.Mono.error(
                      new BaseException(BaseResponseStatus.AI_SERVER_COMMUNICATION_ERROR));
                });
          })
          .bodyToMono(AiQuestionResponse.class)
          .block(); // 동기적으로 사용하기 위해 block()을 사용합니다.
    } catch (HttpClientErrorException e) {
      log.error("AI 서버 요청 중 클라이언트 오류가 발생했습니다. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
      throw new BaseException(BaseResponseStatus.AI_SERVER_COMMUNICATION_ERROR);
    } catch (Exception e) {
      log.error("AI 서버 요청 중 알 수 없는 오류가 발생했습니다.", e);
      throw new BaseException(BaseResponseStatus.AI_SERVER_COMMUNICATION_ERROR);
    }
  }

  /**
   * AI에게 오늘의 멤버 할당을 요청합니다.
   */
  public MemberAssignResponse requestTodayMember(UUID familyId, Map<UUID, Integer> memberAssignedCounts, int pickCount) {
    List<MemberInfo> members = memberAssignedCounts.entrySet().stream()
        .map(entry -> new MemberInfo(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());

    MemberAssignRequest requestDto = MemberAssignRequest.builder()
        .familyId(familyId)
        .members(members)
        .pickCount(pickCount)
        .build();

    try {
      return restClient.post()
          .uri("/api/v1/questions/assign")
          .contentType(MediaType.APPLICATION_JSON)
          .body(requestDto)
          .retrieve()
          .body(MemberAssignResponse.class);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode().value() == 422) {
        log.error("AI 서버 Validation Error: {}", e.getResponseBodyAsString());
        throw new BaseException(BaseResponseStatus.AI_SERVER_VALIDATION_ERROR);
      }
      log.error("AI 서버 요청 중 클라이언트 오류가 발생했습니다.", e);
      throw new BaseException(BaseResponseStatus.AI_SERVER_COMMUNICATION_ERROR);
    }
  }

  /**
   * AI 서버에 답변 분석을 요청합니다.
   */
  public AnswerAnalysis analyzeAnswer(Answer answer, AnswerAnalysisRequest request) {
    log.info("AI 서버에 답변 분석을 요청합니다. Answer ID: {}", answer.getId());

    try {
      AnswerAnalysisResponse response = restClient.post()
          .uri("/api/v1/analysis/answer/api")
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(AnswerAnalysisResponse.class);

      if (response == null) {
        log.error("AI 서버 응답이 비어있습니다.");
        throw new BaseException(BaseResponseStatus.AI_SERVER_COMMUNICATION_ERROR);
      }

      JsonNode categories = objectMapper.valueToTree(response.getCategories());
      JsonNode scores = objectMapper.valueToTree(response.getScores());

      AnswerAnalysis analysis = AnswerAnalysis.createFromAIResponse(answer, response, categories, scores);
      return answerAnalysisRepository.save(analysis);
    } catch (HttpClientErrorException e) {
      log.error("AI 서버 답변 분석 요청 중 클라이언트 오류가 발생했습니다.", e);
      throw new BaseException(BaseResponseStatus.AI_SERVER_COMMUNICATION_ERROR);
    } catch (Exception e) {
      log.error("AI 서버 답변 분석 처리 중 오류가 발생했습니다.", e);
      throw new BaseException(BaseResponseStatus.AI_SERVER_COMMUNICATION_ERROR);
    }
  }
}