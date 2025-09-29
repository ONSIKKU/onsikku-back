package com.onsikku.onsikku_back.domain.ai.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.onsikku.onsikku_back.domain.ai.dto.*;
import com.onsikku.onsikku_back.domain.ai.entity.AnswerAnalysis;
import com.onsikku.onsikku_back.domain.ai.repository.AnswerAnalysisRepository;
import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiRequestService {
  private final RestTemplate restTemplate = new RestTemplate();
  // TODO : application.yml 에서 관리 고려, 요청별 상수 다르게 해야함
  private final String AI_SERVER_URL = "https://editor.swagger.io/api/v1/questions/assign";
  private final AnswerAnalysisRepository answerAnalysisRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   *
   * @param prompt
   * @return
   */
  // AI 에게 질문 생성 요청
  // 한 가족씩, 멤버당 두개의 질문을 생성
  // response : ai 서버한테 받은 결과 반환
  public String requestQuestionGeneration(String prompt) {
    // AI 서버에 질문 생성 요청 보내기
    // 예시: HTTP 클라이언트를 사용하여 AI 서버에 요청을 보냄
    // String aiResponse = httpClient.post("http://ai-server/generate-question", prompt);

    // 여기서는 예시로 고정된 응답을 반환
    String aiResponse = "Generated question based on informations: ";

    return null;
  }


  // AI 에게 멤버 할당 요청 - 매일
  public String requestTodayMember(UUID familyId, Map<UUID, Integer> memberAssignedCounts, int pickCount) {
    // DTO 변환
    List<MemberInfo> members = memberAssignedCounts.entrySet().stream()
        .map(entry -> new MemberInfo(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());

    MemberAssignRequest requestDto = MemberAssignRequest.builder()
        .familyId(familyId)
        .members(members)
        .pickCount(pickCount)
        .build();

    try {
      // 요청 헤더 설정 및 AI 서버에 POST 요청 보내기
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      ResponseEntity<String> response = restTemplate.exchange(
          AI_SERVER_URL,
          HttpMethod.POST,
          new HttpEntity<>(requestDto, headers),
          String.class
      );
      return response.getBody();
    } catch (HttpStatusCodeException ex) {
      if (ex.getStatusCode().value() == 422) {
        throw new IllegalArgumentException(
            "AI 서버 Validation Error: " + ex.getResponseBodyAsString(), ex
        );
      }
      throw ex;
    }
  }


  // 가족 멤버가 질문에 답변 시, AI는 답변이 온 줄 모름
  // 그러므로 AI 서버 API에 답변 분석 요청
  // 반환값 멤버간 친밀도 / 답변 분석 엔티티 저장하기
  public AnswerAnalysis analyzeAnswer(Answer answer, AnswerAnalysisRequest request) {
    // HTTP 요청 생성
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // AI 서버 호출
    AnswerAnalysisResponse response = restTemplate.postForObject(AI_SERVER_URL, new HttpEntity<>(request, headers), AnswerAnalysisResponse.class);

    if (response == null) {
      throw new IllegalStateException("AI 서버 응답이 null 입니다.");
    }

    // 결과 엔티티로 변환 후 저장
    AnswerAnalysis analysis = AnswerAnalysis.builder()
        .answer(answer)
        .analysisModel("default") // 필요시 응답값에 맞춰 수정
        .analysisParameters(response.getAnalysisParameters())
        .analysisPrompt(response.getAnalysisPrompt())
        .analysisRaw(response.getAnalysisRaw())
        .analysisVersion(response.getAnalysisVersion())
        .summary(response.getSummary())
        .categories(objectMapper.valueToTree(response.getCategories()))
        .scores(objectMapper.valueToTree(response.getScores()))
        .build();

    return answerAnalysisRepository.save(analysis);
  }
}
