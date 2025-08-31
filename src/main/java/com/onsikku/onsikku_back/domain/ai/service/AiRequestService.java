package com.onsikku.onsikku_back.domain.ai.service;


import org.springframework.stereotype.Service;

@Service
public class AiRequestService {

  // AI 에게 질문 생성 요청 ( BATCH로? 한번에 모든 가족 vs 1분단위로 1/10 가족씩)
  // + ai 서버한테 받은 결과 반환
  public String requestQuestionGeneration(String prompt) {
    // AI 서버에 질문 생성 요청 보내기
    // 예시: HTTP 클라이언트를 사용하여 AI 서버에 요청을 보냄
    // String aiResponse = httpClient.post("http://ai-server/generate-question", prompt);

    // 여기서는 예시로 고정된 응답을 반환
    String aiResponse = "Generated question based on informations: ";

    return null;
  }

}
