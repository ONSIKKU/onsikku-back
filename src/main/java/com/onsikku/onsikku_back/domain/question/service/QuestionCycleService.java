package com.onsikku.onsikku_back.domain.question.service;


import com.onsikku.onsikku_back.domain.ai.dto.request.AiQuestionRequest;
import com.onsikku.onsikku_back.domain.ai.dto.response.AiQuestionResponse;
import com.onsikku.onsikku_back.domain.ai.service.AiRequestService;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.question.domain.MemberQuestion;
import com.onsikku.onsikku_back.domain.question.domain.Question;
import com.onsikku.onsikku_back.domain.question.domain.QuestionCycle;
import com.onsikku.onsikku_back.domain.question.domain.QuestionStatus;
import com.onsikku.onsikku_back.domain.question.repository.MemberQuestionRepository;
import com.onsikku.onsikku_back.domain.question.repository.QuestionCycleRepository;
import com.onsikku.onsikku_back.domain.question.repository.QuestionRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionCycleService {

  private final QuestionCycleRepository cycleRepository;
  private final MemberRepository memberRepository;
  private final MemberQuestionRepository memberQuestionRepository;
  private final QuestionRepository questionRepository;
  private final AiRequestService aiRequestService;

  @Transactional
  public void getOrGenerateCycleAndAssignQuestionForFamily(Family family, LocalDateTime sendTime) {
    QuestionCycle cycle = findOrCreateOrRefreshCycleForFamily(family);
    Member todayMember = findTodayMemberForFamily(cycle, family);

    // 질문 할당 전, 조건이 맞으면 AI 질문을 생성하여 DB에 먼저 넣어둠
    checkAndPreGenerateAiQuestion(todayMember, family);

    // 질문 할당 로직 수행
    MemberQuestion selectedQuestion = assignFinalQuestion(todayMember, family);

    // 상태 업데이트 및 전송 처리
    selectedQuestion.markAsSent(sendTime, sendTime.plusDays(1)); // 24시간 뒤 마감

    // 사이클 상태 업데이트
    cycle.incrementIndex();
  }

  /**
   * 2주 주기 및 주말 여부를 확인하여, 가족의 최근 질문 기반 AI 파생 질문을 미리 생성합니다.
   */
  private void checkAndPreGenerateAiQuestion(Member member, Family family) {
    List<Integer> allowedLevels = getCurrentAllowedLevels();

    // 1. 주말(Level 3 허용)인지 확인
    // 2. 마지막 AI 질문 생성일로부터 14일이 지났는지 확인
    if (allowedLevels.contains(3) && family.isEligibleForAiQuestion(14)) {
      try {
        AiQuestionResponse response = aiRequestService.requestFamilyQuestionFromRecentQuestions(AiQuestionRequest.of(member));
        String aiContent = response.getContent();

        if (aiContent != null && !aiContent.isBlank()) {
          memberQuestionRepository.save(MemberQuestion.createMemberQuestionFromAiResponse(member,response));

          // 가족의 AI 질문 생성일 업데이트
          family.updateLastAiQuestionDate();
        }
      } catch (Exception e) {
        // 실패해도 배치는 계속되어야 하므로 예외를 던지지 않고 로그만 남김
      }
    }
  }

  private MemberQuestion assignFinalQuestion(Member member, Family family) {
    // 요일에 따른 허용 레벨 설정
    List<Integer> levels = getCurrentAllowedLevels();

    // 이미 생성되어 대기 중인 질문이 있는지 확인
    // 우선순위: 3(가족파생) > 2(개인파생) > 1(일반) 순으로 OrderBy 처리
    return memberQuestionRepository.findTopQuestionNative(member.getId(), QuestionStatus.PENDING.name(), levels)
        .orElseGet(() -> {
          // 적절한 기존 질문이 없으면 템플릿에서 새로 생성
          Question question = questionRepository.findRandomTemplateForMember(member.getId(), levels)
              .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_QUESTION_NOT_FOUND)); // 더 이상 줄 질문이 없음
          MemberQuestion memberQuestion = MemberQuestion.createMemberQuestionFromQuestion(member, family, question);
          return memberQuestionRepository.save(memberQuestion);
        });
  }

  private List<Integer> getCurrentAllowedLevels() {
    DayOfWeek day = LocalDateTime.now().getDayOfWeek();
    if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
      return List.of(3, 4);
    }
    return List.of(1, 2);
  }

  private Member findTodayMemberForFamily(QuestionCycle cycle, Family family) {
    // 오늘 주인공 찾기 (부재 시 다음 사람으로 스킵하는 로직)
    log.info("Finding member for family {}", family.getId());
    Member todayMember = null;
    while (todayMember == null && !cycle.isFinished()) {
      UUID memberId = cycle.getTodayMemberId();
      todayMember = memberRepository.findById(memberId).orElse(null);

      if (todayMember == null) {
        cycle.incrementIndex();     // 존재하지 않는 멤버면 인덱스만 올리고 다음 루프
        if (cycle.isFinished()) {   // 만약 스킵했는데 사이클이 끝났다면 다시 갱신 시도
          cycle.refreshCycle(memberRepository.findByFamily_Id(family.getId()));
        }
      }
    }
    return todayMember;
  }

  private QuestionCycle findOrCreateOrRefreshCycleForFamily(Family family) {
    // 활성화된 사이클 조회, 없으면 새로 생성
    log.info("Finding cycle for family {}", family.getId());
    QuestionCycle cycle = cycleRepository.findByFamily_Id(family.getId())
        .orElseGet(() -> QuestionCycle.createNewCycle(family, memberRepository.findByFamily_Id(family.getId())));

    // 사이클이 끝났으면 새로 갱신
    if(cycle.isFinished()) {
      cycle.refreshCycle(memberRepository.findByFamily_Id(family.getId()));
    }
    return cycle;
  }
}
