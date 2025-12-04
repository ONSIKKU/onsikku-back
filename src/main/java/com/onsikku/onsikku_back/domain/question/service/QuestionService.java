package com.onsikku.onsikku_back.domain.question.service;



import com.onsikku.onsikku_back.domain.ai.domain.AnswerAnalysis;
import com.onsikku.onsikku_back.domain.ai.dto.request.AiQuestionRequest;
import com.onsikku.onsikku_back.domain.ai.dto.request.AnswerAnalysisDetails;
import com.onsikku.onsikku_back.domain.ai.dto.response.AiQuestionResponse;
import com.onsikku.onsikku_back.domain.ai.dto.response.MemberAssignResponse;
import com.onsikku.onsikku_back.domain.ai.repository.AnswerAnalysisRepository;
import com.onsikku.onsikku_back.domain.ai.service.AiRequestService;
import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerResponse;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.answer.repository.CommentRepository;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.member.service.FamilyService;
import com.onsikku.onsikku_back.domain.question.domain.*;
import com.onsikku.onsikku_back.domain.question.domain.enums.AssignmentState;
import com.onsikku.onsikku_back.domain.question.dto.QuestionDetails;
import com.onsikku.onsikku_back.domain.question.dto.QuestionResponse;
import com.onsikku.onsikku_back.domain.question.repository.QuestionAssignmentRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.repository.QuestionInstanceRepository;
import com.onsikku.onsikku_back.domain.question.repository.QuestionTemplateRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import jakarta.persistence.EntityManager;
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
    private final QuestionAssignmentRepository questionAssignmentRepository;
    private final QuestionInstanceRepository questionInstanceRepository;
    private final QuestionTemplateRepository questionTemplateRepository;
    private final AnswerRepository answerRepository;
    private final FamilyService familyService;
    private final AiRequestService aiRequestService;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final EntityManager entityManager;
    private final AnswerAnalysisRepository answerAnalysisRepository;

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
        List<QuestionAssignment> remindTargets = questionAssignmentRepository.findAssignmentsForSentAtAndSentState(remindCutoff);
        int remindedCount = 0;

        for (QuestionAssignment qa : remindTargets) {
            if (qa.getReminderCount() < 2) { // TODO : 리마인드는 2회까지만 허용
                qa.markAsReminded();
                // TODO: 외부 알림 서비스(Push/SMS) 호출 로직 추가
                remindedCount++;
            }
        }
        questionAssignmentRepository.saveAll(remindTargets);
        List<QuestionAssignment> expireTargets = questionAssignmentRepository.findAssignmentsForSentAtAndSentState(expireCutoff);
        int expiredCount = expireTargets.size();
        for (QuestionAssignment qa : expireTargets) {
            qa.markAsExpired();
        }
        questionAssignmentRepository.saveAll(expireTargets);
        log.info("[Scheduler] 미답변 할당 관리 완료. 총 처리 할당 수: {}", remindedCount + expiredCount);
        return remindedCount + expiredCount;
    }
    /**
     * [Scheduler] 새로운 질문 생성 및 할당 필요성을 검사하고, 필요 시 AI를 호출하여 질문을 생성합니다.
     * @param family 질문을 생성할 가족
     */
    public void generateAndAssignQuestionForFamily(Family family) {
        if (!isNewQuestionNeeded(family)) { // 새로운 질문이 필요하지 않다면 종료
            log.info("가족 ID {}는 새로운 질문 생성이 필요하지 않습니다.", family.getId());
            return;
        }
        // 오늘의 주인공 선정
        Map<UUID, Integer> memberAssignedCounts = familyService.getMemberAssignedCounts(family);
        log.info("오늘의 주인공 선정을 시작합니다.");
        MemberAssignResponse memberAssignResponse = aiRequestService.requestTodayMember(family.getId(), memberAssignedCounts, 1);
        log.info("오늘의 주인공 선정 완료: {}", memberAssignResponse.getMemberIds());

        // TODO : 꼬리질문이 1회차인지 알기위해 엔티티에 boolean 추가해야하나? + tryGenerateFollowUpQuestion 검증로직 구현해야함
        log.info("가족 ID {}의 새로운 질문 생성을 시작합니다.", family.getId());
        //tryGenerateFollowUpQuestion(family, memberAssignResponse);        // 꼬리 질문 시도
        tryGenerateTemplateQuestion(family, memberAssignResponse);        // 템플릿 질문 시도
    }


    // ----------------------------------------------------------------------
    // API Methods
    // ----------------------------------------------------------------------
    /**
     * 특정 가족의 가장 최신인 질문 할당 리스트를 조회하는 로직
     * 1. 가장 오래된 미답변 질문 INSTANCE 우선 조회
     * 2. 미답변 질문이 없다면, 가장 최신 질문 INSTANCE 조회
     * 3. 해당 INSTANCE ID가 포함된 모든 질문 ASSIGNMENT 조회
     * 4. 본인 멤버의 질문은 조회 시점에 읽음 처리
     * 5. 조회된 질문 세트 반환
     * @param member 질문을 조회할 가족의 멤버
     * @return 조회된 질문 세트 목록
     */
    @Transactional
    public QuestionResponse findTodayQuestionInstance(Member member) {
        Family family = member.getFamily();
        // 가장 오래된 미답변 질문의 ID를 먼저 찾는다 (LIMIT 1)
        Optional<QuestionInstance> targetInstance = questionAssignmentRepository
            .findOldestUnansweredInstance(family.getId(), PageRequest.of(0, 1))
            .stream().findFirst();
        log.info("가족 ID {}의 미답변 질문 인스턴스 ID 조회 완료 : {}", family.getId(), targetInstance.map(QuestionInstance::getId).orElse(null));

        if (targetInstance.isEmpty()) {
            log.info("미답변 질문이 없습니다. 가장 최신 질문을 조회합니다.");
            // 미답변 질문 ID가 없다면, 가장 최신 질문의 ID를 찾는다 (LIMIT 1)
            targetInstance = questionInstanceRepository
                .findMostRecentInstance(family.getId(), LocalDateTime.now(), PageRequest.of(0, 1))
                .stream().findFirst();
        }
        if (targetInstance.isEmpty()) {
            log.info("가족 ID {}의 질문 인스턴스가 없습니다. 빈 목록을 반환합니다.", family.getId());
            return QuestionResponse.builder().questionDetails(null).build();
        }

        // 최종적으로 찾은 ID가 있다면, 해당 ID로 질문 세트 전체를 조회한다. 없다면 빈 목록을 반환한다.
        QuestionInstance instance = targetInstance.get();
        UUID instanceId = instance.getId();
        log.info("최종 질문 인스턴스 ID 조회 완료: {}", instanceId);
        List<QuestionAssignment> assignments = questionAssignmentRepository.findAllByInstanceId(instanceId);
        for (QuestionAssignment qa : assignments) {
            if(qa.getMember().getId().equals(member.getId())) {
                log.info("멤버 ID {}의 질문 할당을 읽음 처리합니다. 질문 할당 ID: {}", member.getId(), qa.getId());
                qa.markAsRead();
            }
        }
        return QuestionResponse.builder()
            .questionDetails(QuestionDetails.from(instance,
                assignments,
                answerRepository.findAllByQuestionInstanceId(instanceId).stream().map(AnswerResponse::from).toList(),
                commentRepository.findAllByQuestionInstanceIdWithParentOrderByCreatedAtDesc(instanceId))
            )
            .familyMembers(memberRepository.findAllByFamily_Id(family.getId()))
            .build();
    }
    // 특정 질문 인스턴스의 상세 정보를 조회합니다.
    @Transactional(readOnly = true)
    public QuestionResponse findQuestionDetails(Member member, UUID questionInstanceId) {
        QuestionInstance instance = questionInstanceRepository.findById(questionInstanceId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.QUESTION_INSTANCE_NOT_FOUND));
        if (!instance.getFamily().getId().equals(member.getFamily().getId())) {
            throw new BaseException(BaseResponseStatus.INVALID_FAMILY_MEMBER);
        }

        return QuestionResponse.builder()
            .questionDetails(QuestionDetails.from(instance,
                // 할당 목록에서 Member 리스트 추출
                questionAssignmentRepository.findAllByInstanceId(questionInstanceId),
                answerRepository.findAllByQuestionInstanceId(questionInstanceId).stream().map(AnswerResponse::from).toList(),
                commentRepository.findAllByQuestionInstanceIdWithParentOrderByCreatedAtDesc(questionInstanceId))
            )
            .build();
    }

    // 특정 월의 모든 질문 인스턴스를 조회하고, 각 인스턴스에 대한 모든 답변 정보를 담아 반환합니다.
    @Transactional(readOnly = true)
    public QuestionResponse findMonthlyQuestions(Family family, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        // 해당 월의 모든 QuestionInstance 조회
        log.info("가족 ID {}의 {} ~ {} 범위의 질문 인스턴스 조회를 시작합니다.", family.getId(), yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(LocalTime.MAX));
        List<QuestionInstance> questionInstances = questionInstanceRepository.findQuestionsByFamilyIdAndDateTimeRange(family.getId(), yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(LocalTime.MAX));
        if (questionInstances.isEmpty()) {
            log.info("해당 월 질문 인스턴스가 없습니다.");
            return QuestionResponse.builder().questionDetailsList(List.of()).build();
        }

        // 인스턴스 ID 리스트 추출 및 해당 ID들로 모든 QuestionAssignment 조회
        List<UUID> instanceIds = questionInstances.stream().map(QuestionInstance::getId).toList();
        List<QuestionAssignment> allAssignments = questionAssignmentRepository.findAllByInstanceIdsWithMembers(instanceIds);
        // 인스턴스 ID 별로 할당 리스트를 매핑한 Map 생성
        Map<UUID, List<QuestionAssignment>> assignmentsMap = allAssignments.stream()
            .collect(Collectors.groupingBy(assignment -> assignment.getQuestionInstance().getId()));

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
    // private Methods
    // ----------------------------------------------------------------------

    // TODO : 질문 필요 유무 판단 로직 개선 필요
    private boolean isNewQuestionNeeded(Family family) {
        // 가장 최신 질문 인스턴스 조회
        List<QuestionAssignment> assignments = questionAssignmentRepository.findMostRecentUnansweredAssignmentId(family.getId(), LocalDateTime.now());
        log.info("가족 ID {}의 최신 질문 할당 조회 완료. 총 {}개", family.getId(), assignments.size());
        Optional<QuestionAssignment> latestAssignment = assignments.stream().findFirst();
        if (latestAssignment.isEmpty()) return true;

        // 모든 질문이 답변된 경우에만 새 질문이 필요
        return assignments.stream().allMatch(assignment -> assignment.getState() == AssignmentState.ANSWERED);
    }

    // 꼬리 질문 생성 시도
    private void tryGenerateFollowUpQuestion(Family family, MemberAssignResponse memberAssignResponse) {
        Optional<Answer> answer = answerRepository.findTopByMember_Family_IdOrderByCreatedAtDesc(family.getId());
        if (answer.isEmpty()) return;
        log.info("가족 ID {}의 최근 답변 조회 완료. ", family.getId());

        Answer recentAnswer = answer.get();
        QuestionInstance instance = questionInstanceRepository.findByIdWithQuestionTemplate(recentAnswer.getQuestionInstance().getId())
            .orElseThrow(() -> new BaseException(BaseResponseStatus.QUESTION_INSTANCE_NOT_FOUND));

        log.info("가족 ID {}의 꼬리 질문 생성을 시도합니다. (이전 답변 ID: {})", family.getId(), recentAnswer.getId());

        // AI에게 이전 질문/답변을 알려주며 꼬리 질문을 요청하는 DTO 구성
        AnswerAnalysis analysis = answerAnalysisRepository.findByAnswer(recentAnswer);
        AiQuestionResponse response = aiRequestService.requestQuestionGeneration(AiQuestionRequest.forFollowUp(instance, AnswerAnalysisDetails.fromAnswerAnalysis(analysis)));
        saveInstanceAndAssignments(response, family, memberAssignResponse, true);
    }
    // 템플릿 질문 생성 시도
    private void tryGenerateTemplateQuestion(Family family, MemberAssignResponse memberAssignResponse) {
        // 해당 가족이 최근 N일 내에 사용하지 않은 템플릿 중 하나를 무작위로 조회
        // QuestionTemplate을 반환하는 쿼리는 QuestionTemplateRepository에 속한다 : SRP - 책임 분리 원칙
        List<QuestionTemplate> templates = questionTemplateRepository
            .findUnusedTemplatesRecentlyByFamily(family.getId(), LocalDateTime.now().minusMonths(2L));
        if (templates.isEmpty()) {
            log.warn("가족 ID {}에 사용할 수 있는 템플릿 질문이 없습니다. 질문 생성 프로세스를 종료합니다.", family.getId());
            return;
        }
        // DB 부하 없이, 애플리케이션 메모리에서 무작위 선택
        Collections.shuffle(templates);
        QuestionTemplate template = templates.getFirst();
        log.info("가족 ID {}의 템플릿 질문 생성을 시도합니다. 템플릿 ID: {}", family.getId(), template.getId());

        // 템플릿 내용을 기반으로 질문을 생성하도록 AI에 요청
        AiQuestionRequest request = AiQuestionRequest.fromTemplate(template);
        AiQuestionResponse response = aiRequestService.requestQuestionGeneration(request);
        // 생성된 질문 인스턴스에 템플릿 ID 기록 후 저장
        response.setUsedTemplateId(template.getId());
        saveInstanceAndAssignments(response, family, memberAssignResponse, false);
    }

    // 전달받은 AI 응답과 주인공 정보, 꼬리질문 여부를 바탕으로 QuestionInstance, QuestionAssignment를 생성, 저장
    @Transactional
    public void saveInstanceAndAssignments(AiQuestionResponse response, Family family, MemberAssignResponse memberAssignResponse, boolean isFollowUp) {
        QuestionInstance questionInstance = QuestionInstance.generateByAI(response, family, isFollowUp);

        // 만약 템플릿 질문이었다면, 사용된 템플릿 정보 연결
        if (response.getUsedTemplateId() != null) {
            questionInstance.setTemplate(entityManager.getReference(QuestionTemplate.class, response.getUsedTemplateId()));
        }
        questionInstanceRepository.save(questionInstance);

        // QuestionInstance 기반, QuestionAssignment 생성 및 저장
        List<QuestionAssignment> assignments = new ArrayList<>();
        for (UUID memberId : memberAssignResponse.getMemberIds()) {
            QuestionAssignment assignment = QuestionAssignment.createAndAssignTo(questionInstance, memberRepository.findById(memberId).orElse(null), family);
            assignment.markAsSent(LocalDateTime.now().plusWeeks(1L));
            assignments.add(assignment);
        }
        questionAssignmentRepository.saveAll(assignments);
    }

    // 질문 삭제
    @Transactional
    public void deleteQuestionsByFamilyId(Family family) {
        List<QuestionInstance> instances = questionInstanceRepository.findAllByFamily(family);
        log.info("삭제한 answers : {}", answerRepository.deleteAllByQuestionInstanceIn(instances));
        log.info("삭제한 comments : {}", commentRepository.deleteAllByQuestionInstanceIn(instances));
        log.info("삭제한 assignments : {}", questionAssignmentRepository.deleteAllByFamily(family));
        log.info("삭제한 instances : {}", questionInstanceRepository.deleteAllByFamilyId(family.getId()));
    }
}