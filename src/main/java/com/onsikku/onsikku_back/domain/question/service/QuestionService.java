package com.onsikku.onsikku_back.domain.question.service;


import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.answer.repository.CommentRepository;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.question.domain.*;
import com.onsikku.onsikku_back.domain.question.dto.QuestionDetails;
import com.onsikku.onsikku_back.domain.question.dto.QuestionResponse;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.repository.MemberQuestionRepository;
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
        List<MemberQuestion> expireTargets = null; //memberQuestionRepository.findAllByFamily(expireCutoff);
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
     * 특정 가족의 오늘의 질문을 조회합니다.
     * @param member 질문을 조회할 가족의 멤버
     * @return 조회된 질문 세트 목록
     */
    @Transactional
    public QuestionResponse getTodayMemberQuestion(Member member) {
        List<MemberQuestion> oneMemberQuestion = memberQuestionRepository.findTodayQuestionForFamily(member.getFamily().getId(), LocalDateTime.now(), PageRequest.of(0,1));
        if (oneMemberQuestion.isEmpty()) {
            throw new BaseException(BaseResponseStatus.MEMBER_QUESTION_NOT_FOUND);
        }
        MemberQuestion memberQuestion = oneMemberQuestion.get(0);
        Answer answer = answerRepository.findByMemberQuestion_Id(memberQuestion.getId()).orElse(null);
        if (answer == null) {
            return QuestionResponse.builder()
                .questionDetails(QuestionDetails.fromOnlyMemberQuestion(memberQuestion))
                .familyMembers(memberRepository.findAllByFamily_Id(member.getFamily().getId()))
                .build();
        }
        return QuestionResponse.builder()
            .questionDetails(QuestionDetails.from(memberQuestion, answer, commentRepository.findAllByAnswerIdWithParentOrderByCreatedAtDesc(memberQuestion.getId())))
            .familyMembers(memberRepository.findAllByFamily_Id(member.getFamily().getId()))
            .build();
    }

    // 특정 질문 인스턴스의 상세 정보를 조회합니다.
    @Transactional(readOnly = true)
    public QuestionResponse findQuestionDetails(Member member, UUID memberQuestionId) {
        MemberQuestion memberQuestion = memberQuestionRepository.findByIdWithMember(memberQuestionId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_QUESTION_NOT_FOUND));
        if (!memberQuestion.getFamily().getId().equals(member.getFamily().getId())) {
            throw new BaseException(BaseResponseStatus.INVALID_FAMILY_MEMBER);
        }
        Answer answer = answerRepository.findByMemberQuestion_Id(memberQuestion.getId()).orElse(null);
        if (answer == null) {
            return QuestionResponse.builder()
                .questionDetails(QuestionDetails.fromOnlyMemberQuestion(memberQuestion))
                .familyMembers(memberRepository.findAllByFamily_Id(member.getFamily().getId()))
                .build();
        }
        return QuestionResponse.builder()
            .questionDetails(QuestionDetails.from(memberQuestion, answer, commentRepository.findAllByAnswerIdWithParentOrderByCreatedAtDesc(memberQuestionId)))
            .build();
    }

    // 특정 월의 모든 질문 인스턴스를 조회합니다.
    @Transactional(readOnly = true)
    public QuestionResponse findMonthlyQuestions(Family family, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        log.info("가족 ID {}의 {} ~ {} 범위의 질문 조회를 시작합니다.", family.getId(), yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(LocalTime.MAX));
        List<MemberQuestion> memberQuestions = memberQuestionRepository.findQuestionsByFamilyIdAndDateTimeRange(family.getId(), yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(LocalTime.MAX));
        if (memberQuestions.isEmpty()) {
            log.info("해당 월에 질문이 없습니다.");
            return QuestionResponse.builder().questionDetailsList(List.of()).build();
        }

        // 리스트를 DTO로 변환 후 반환
        return QuestionResponse.builder()
            .questionDetailsList(memberQuestions.stream().map(memberQuestion -> QuestionDetails.fromMemberQuestion(memberQuestion)).toList())
            .totalQuestionCount(memberQuestions.size())
            .answeredQuestionCount(123)     // TODO: 답변된 질문 수 계산 로직 추가
            .totalReactionCount(123)
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