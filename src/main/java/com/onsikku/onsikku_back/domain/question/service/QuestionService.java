package com.onsikku.onsikku_back.domain.question.service;


import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.domain.Comment;
import com.onsikku.onsikku_back.domain.answer.domain.Reaction;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.answer.repository.CommentRepository;
import com.onsikku.onsikku_back.domain.answer.repository.ReactionRepository;
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
    private final ReactionRepository reactionRepository;

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
        // 본인이 주인공이고, 질문 상태가 SENT면 markAsRead
        if(memberQuestion.getMember().getId().equals(member.getId()) && memberQuestion.getQuestionStatus() == QuestionStatus.SENT) {
            memberQuestion.markAsRead();
        }
        QuestionResponse response = assembleQuestionResponse(member, memberQuestion);
        response.setFamilyMembers(memberRepository.findAllByFamily_Id(member.getFamily().getId()));
        return response;
    }

    // 특정 질문의 상세 정보를 조회합니다.
    @Transactional(readOnly = true)
    public QuestionResponse findQuestionDetails(Member member, UUID memberQuestionId) {
        MemberQuestion memberQuestion = memberQuestionRepository.findByIdWithMember(memberQuestionId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_QUESTION_NOT_FOUND));
        if (!memberQuestion.getFamily().getId().equals(member.getFamily().getId())) {
            throw new BaseException(BaseResponseStatus.INVALID_FAMILY_MEMBER);
        }
        return assembleQuestionResponse(member, memberQuestion);
    }

    // 특정 월의 할당된 모든 질문을 조회합니다.
    @Transactional(readOnly = true)
    public QuestionResponse findMonthlyQuestions(Family family, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
        List<MemberQuestion> memberQuestions = memberQuestionRepository.findQuestionsByFamilyIdAndDateTimeRange(family.getId(), start, end, LocalDateTime.now());

        if (memberQuestions.isEmpty()) {
            return QuestionResponse.builder()
                .questionDetailsList(List.of())
                .build();
        }
        int answeredQuestionCount = (int) memberQuestions.stream()
            .filter(MemberQuestion::isAnswered)
            .count();

        int totalReactionCount = reactionRepository.countMonthlyReactions(family.getId(), start, end);
        // 리스트를 DTO로 변환 후 반환
        return QuestionResponse.builder()
            .questionDetailsList(memberQuestions.stream().map(memberQuestion -> QuestionDetails.fromMemberQuestion(memberQuestion)).toList())
            .totalQuestionCount(memberQuestions.size())
            .answeredQuestionCount(answeredQuestionCount)
            .totalReactionCount(totalReactionCount)
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

    private QuestionResponse assembleQuestionResponse(Member member, MemberQuestion memberQuestion) {
        UUID memberQuestionId = memberQuestion.getId();
        Answer answer = answerRepository.findByMemberQuestion_Id(memberQuestionId).orElse(null);
        if (answer == null) {
            return QuestionResponse.builder()
                .questionDetails(QuestionDetails.fromMemberQuestion(memberQuestion))
                .build();
        }
        // 리액션 리스트 조회
        List<Reaction> reactions = reactionRepository.findAllByAnswer_Id(answer.getId());   // TODO : 성능 개선 가능
        List<Comment> comments = commentRepository.findAllByAnswerIdWithParentOrderByCreatedAtDesc(answer.getId());
        return QuestionResponse.builder()
            .questionDetails(QuestionDetails.from(memberQuestion, answer, comments, reactions, member.getId()))
            .build();
    }
}