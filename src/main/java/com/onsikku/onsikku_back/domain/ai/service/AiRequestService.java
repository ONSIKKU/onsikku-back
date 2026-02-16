package com.onsikku.onsikku_back.domain.ai.service;

import com.onsikku.onsikku_back.domain.ai.dto.request.AiQuestionRequest;
import com.onsikku.onsikku_back.domain.ai.dto.response.AiQuestionResponse;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.question.domain.MemberQuestion;
import com.onsikku.onsikku_back.domain.question.repository.MemberQuestionRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class AiRequestService {

  private final AiClient aiClient;
  private final MemberQuestionRepository memberQuestionRepository;
  private final MemberRepository memberRepository;

  /**
   * 주에 한번 AI 서버에 가족 파생 질문 생성을 요청하고, 응답을 DTO 객체로 반환합니다.
   */
  @Async("aiTaskExecutor")    // AsyncConfig에 정의된 aiTaskExecutor 사용
  @Transactional
  public AiQuestionResponse requestFamilyReportGeneration(AiQuestionRequest requestDto) {
    return aiClient.postForAiRequest("/api/v1/summary", requestDto);
  }

  /**
   * 답변 이후 AI 서버에 개인 파생 질문 생성을 요청 후 저장합니다.
   */
  @Async("aiTaskExecutor")    // AsyncConfig에 정의된 aiTaskExecutor 사용
  @Transactional
  public void requestPersonalQuestionGeneration(AiQuestionRequest requestDto) {
    AiQuestionResponse response = aiClient.postForAiRequest("/api/v1/questions/generate/personal", requestDto);
    // AI 응답 검증 (회원 ID)
    Member targetMember = memberRepository.findMemberWithFamily(response.getMemberId())
        .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));
    // 개인 파생 질문 저장
    memberQuestionRepository.save(MemberQuestion.createMemberQuestionFromAiResponse(targetMember, response));
  }

  /**
   * 주에 한번 AI 서버에 가족 파생 질문 생성을 요청하고, 응답을 DTO 객체로 반환합니다.
   */
  @Async("aiTaskExecutor")    // AsyncConfig에 정의된 aiTaskExecutor 사용
  @Transactional
  public AiQuestionResponse requestFamilyQuestionFromBaseQuestion(AiQuestionRequest requestDto) {
    return aiClient.postForAiRequest("/api/v1/questions/generate/family", requestDto);
  }

  /**
   * 주에 한번 AI 서버에 가족 파생 질문 생성을 요청하고, 응답을 DTO 객체로 반환합니다.
   */
  @Async("aiTaskExecutor")    // AsyncConfig에 정의된 aiTaskExecutor 사용
  @Transactional
  public AiQuestionResponse requestFamilyQuestionFromRecentQuestions(AiQuestionRequest requestDto) {
    return aiClient.postForAiRequest("/api/v1/questions/generate/family-recent", requestDto);
  }

  /**
   * 회원 삭제시 AI 서버에 해당 회원과 관련된 질문 삭제 요청을 보냅니다.
   */
  @Transactional
  public int requestMemberDataDeletion(AiQuestionRequest requestDto) {
    return aiClient.postForAiRequest("/api/v1/members/delete", requestDto).getDeletedCount();
  }
}