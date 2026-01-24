package com.onsikku.onsikku_back.domain.question.service;


import com.onsikku.onsikku_back.domain.ai.service.AiRequestService;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerResponse;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.answer.repository.CommentRepository;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.member.service.FamilyService;
import com.onsikku.onsikku_back.domain.question.domain.*;
import com.onsikku.onsikku_back.domain.question.dto.QuestionDetails;
import com.onsikku.onsikku_back.domain.question.dto.QuestionResponse;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.repository.MemberQuestionRepository;
import com.onsikku.onsikku_back.domain.question.repository.QuestionRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {
    private final MemberQuestionRepository memberQuestionRepository;
    private final AnswerRepository answerRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    // ----------------------------------------------------------------------
    // Scheduler Methods
    // ----------------------------------------------------------------------
    /**
     * [Scheduler] 미답변 할당을 찾아 리마인드하거나, 만료 기한이 지난 할당을 파기 처리합니다.
     * 이 메서드는 QuestionScheduler에서 주기적으로 호출됩니다.
     * @return 처리된 할당(리마인드 + 파기)의 총 개수
     */
    @Transactional
    public int remindOrExpireAssignments() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime remindCutoff = today.minusDays(2);    // 리마인드 기한
        LocalDateTime expireCutoff = today.minusDays(7);    // 파기 기한
        log.info("[Scheduler] 미답변 할당 관리 시작");
        List<MemberQuestion> remindTargets = memberQuestionRepository.findAllByFamily(Family.builder().build());
        int remindedCount = 0;

        for (MemberQuestion qa : remindTargets) {
            if (qa.getReminderCount() < 2) { // TODO : 리마인드는 2회까지만 허용
                qa.markAsReminded();
                // TODO: 외부 알림 서비스(Push/SMS) 호출 로직 추가
                remindedCount++;
            }
        }
        memberQuestionRepository.saveAll(remindTargets);
        List<MemberQuestion> expireTargets = memberQuestionRepository.findAssignmentsForSentAtAndSentState(expireCutoff);
        int expiredCount = expireTargets.size();
        for (MemberQuestion qa : expireTargets) {
            qa.markAsExpired();
        }
        memberQuestionRepository.saveAll(expireTargets);
        log.info("[Scheduler] 미답변 할당 관리 완료. 총 처리 할당 수: {}", remindedCount + expiredCount);
        return remindedCount + expiredCount;
    }


    // ----------------------------------------------------------------------
    // API Methods
    // ----------------------------------------------------------------------
    /**
     * 특정 가족의 가장 최신인 질문 할당 리스트를 조회하는 로직
     * 1. 가장 오래된 미답변 member_question 우선 조회
     * 2. 미답변 질문이 없다면, 가장 최신 member_question 조회
     * 3. 해당 INSTANCE ID가 포함된 모든 질문 ASSIGNMENT 조회
     * 4. 본인 멤버의 질문은 조회 시점에 읽음 처리
     * 5. 조회된 질문 세트 반환
     * @param member 질문을 조회할 가족의 멤버
     * @return 조회된 질문 세트 목록
     */
    @Transactional
    public QuestionResponse getTodayMemberQuestion(Member member) {
        Family family = member.getFamily();
        MemberQuestion memberQuestion = memberQuestionRepository.find(member.getId());
        // 가장 오래된 미답변 질문의 ID를 먼저 찾는다 (LIMIT 1)
        Optional<MemberQuestion> targetInstance = memberQuestionRepository
            .findOldestUnansweredInstance(family.getId(), PageRequest.of(0, 1))
            .stream().findFirst();
        log.info("가족 ID {}의 미답변 질문 인스턴스 ID 조회 완료 : {}", family.getId(), targetInstance.map(MemberQuestion::getId).orElse(null));

        if (targetInstance.isEmpty()) {
            log.info("미답변 질문이 없습니다. 가장 최신 질문을 조회합니다.");
            // 미답변 질문 ID가 없다면, 가장 최신 질문의 ID를 찾는다 (LIMIT 1)
        }
        if (targetInstance.isEmpty()) {
            log.info("가족 ID {}의 질문 인스턴스가 없습니다. 빈 목록을 반환합니다.", family.getId());
            return QuestionResponse.builder().questionDetails(null).build();
        }

        // 최종적으로 찾은 ID가 있다면, 해당 ID로 질문 세트 전체를 조회한다. 없다면 빈 목록을 반환한다.
        MemberQuestion memberQuestion1 = targetInstance.get();
        UUID instanceId = memberQuestion1.getId();
        log.info("최종 질문 인스턴스 ID 조회 완료: {}", instanceId);
        List<MemberQuestion> assignments = memberQuestionRepository.findAllByInstanceId(instanceId);
        for (MemberQuestion qa : assignments) {
            if(qa.getMember().getId().equals(member.getId())) {
                log.info("멤버 ID {}의 질문 할당을 읽음 처리합니다. 질문 할당 ID: {}", member.getId(), qa.getId());
                qa.markAsRead();
            }
        }
        return QuestionResponse.builder()
            .questionDetails(QuestionDetails.from(memberQuestion1,
                assignments,
                answerRepository.findAllByQuestionInstanceId(instanceId).stream().map(AnswerResponse::from).toList(),
                commentRepository.findAllByAnswerIdWithParentOrderByCreatedAtDesc(instanceId))
            )
            .familyMembers(memberRepository.findAllByFamily_Id(family.getId()))
            .build();
    }
    // 특정 질문 인스턴스의 상세 정보를 조회합니다.
    @Transactional(readOnly = true)
    public QuestionResponse findQuestionDetails(Member member, UUID questionInstanceId) {
        MemberQuestion memberQuestion = memberQuestionRepository.findById(questionInstanceId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_QUESTION_NOT_FOUND));
        if (!memberQuestion.getFamily().getId().equals(member.getFamily().getId())) {
            throw new BaseException(BaseResponseStatus.INVALID_FAMILY_MEMBER);
        }

        return QuestionResponse.builder()
            .questionDetails(QuestionDetails.from(memberQuestion,
                // 할당 목록에서 Member 리스트 추출
                memberQuestionRepository.findAllByInstanceId(questionInstanceId),
                answerRepository.findAllByQuestionInstanceId(questionInstanceId).stream().map(AnswerResponse::from).toList(),
                commentRepository.findAllByAnswerIdWithParentOrderByCreatedAtDesc(questionInstanceId))
            )
            .build();
    }

    // 특정 월의 모든 질문 인스턴스를 조회하고, 각 인스턴스에 대한 모든 답변 정보를 담아 반환합니다.
    @Transactional(readOnly = true)
    public QuestionResponse findMonthlyQuestions(Family family, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        // 해당 월의 모든 QuestionInstance 조회
        log.info("가족 ID {}의 {} ~ {} 범위의 질문 인스턴스 조회를 시작합니다.", family.getId(), yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(LocalTime.MAX));
        List<MemberQuestion> questionInstances = memberQuestionRepository.findQuestionsByFamilyIdAndDateTimeRange(family.getId(), yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(LocalTime.MAX));
        if (questionInstances.isEmpty()) {
            log.info("해당 월 질문 인스턴스가 없습니다.");
            return QuestionResponse.builder().questionDetailsList(List.of()).build();
        }

        // QuestionInstance 리스트를 DTO로 변환 후 반환
        return QuestionResponse.builder()
            .questionDetailsList(
                questionInstances.stream()          // 각 인스턴스에 해당하는 할당 리스트를 매핑 후 DTO 변환 (Map 활용)
                .map(instance -> QuestionDetails.fromInstanceAndAssignments(instance, assignmentsMap.getOrDefault(instance.getId(), List.of())))
                .toList()
            )
            .build();
    }

    // ----------------------------------------------------------------------
    // Test Only Methods
    // ----------------------------------------------------------------------

    // 질문 삭제
    @Transactional
    public void deleteFamilyData(UUID familyId) {
        commentRepository.deleteByAnswerMemberQuestionFamilyId(familyId);
        answerRepository.deleteByMemberQuestionFamilyId(familyId);
        memberQuestionRepository.deleteByFamilyIdBulk(familyId);
        log.info("가족 ID {} 관련 모든 데이터(댓글, 답변, 질문) 삭제 완료", familyId);
    }
}