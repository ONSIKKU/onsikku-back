package com.onsikku.onsikku_back.domain.question.service;



import com.onsikku.onsikku_back.domain.ai.dto.request.AiQuestionRequest;
import com.onsikku.onsikku_back.domain.ai.dto.response.AiQuestionResponse;
import com.onsikku.onsikku_back.domain.ai.dto.response.MemberAssignResponse;
import com.onsikku.onsikku_back.domain.ai.service.AiRequestService;
import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.member.service.FamilyService;
import com.onsikku.onsikku_back.domain.question.domain.*;
import com.onsikku.onsikku_back.domain.question.domain.enums.AssignmentState;
import com.onsikku.onsikku_back.domain.question.dto.QuestionDetails;
import com.onsikku.onsikku_back.domain.question.dto.QuestionRequest;
import com.onsikku.onsikku_back.domain.question.dto.QuestionResponse;
import com.onsikku.onsikku_back.domain.question.repository.QuestionAssignmentRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.repository.QuestionInstanceRepository;
import com.onsikku.onsikku_back.domain.question.repository.QuestionTemplateRepository;
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


    /**
     * 특정 가족의 가장 최신인 질문 할당 리스트를 조회하는 로직
     * 1. 가장 오래된 미답변 질문 INSTANCE 우선 조회
     * 2. 미답변 질문이 없다면, 가장 최신 질문 INSTANCE 조회
     * 3. 해당 INSTANCE ID가 포함된 모든 질문 ASSIGNMENT 조회
     * @param member 질문을 조회할 가족의 멤버
     * @return 조회된 질문 세트 목록
     */
    @Transactional(readOnly = true)
    public List<QuestionAssignment> findQuestions(Member member) {
        Family family = member.getFamily();
        // 가장 오래된 미답변 질문의 ID를 먼저 찾는다 (LIMIT 1)
        Optional<UUID> targetInstanceId = questionAssignmentRepository
            .findOldestUnansweredInstanceId(family.getId(), AssignmentState.SENT, PageRequest.of(0, 1))
            .stream().findFirst();
        log.info("가족 ID {}의 미답변 질문 인스턴스 ID 조회 완료", family.getId());

        if (targetInstanceId.isEmpty()) {
            log.info("미답변 질문이 없습니다. 가장 최신 질문을 조회합니다.");
            // 미답변 질문 ID가 없다면, 가장 최신 질문의 ID를 찾는다 (LIMIT 1)
            targetInstanceId = questionInstanceRepository
                .findMostRecentInstanceId(family.getId(), LocalDateTime.now(), PageRequest.of(0, 1))
                .stream().findFirst();
        }

        // 최종적으로 찾은 ID가 있다면, 해당 ID로 질문 세트 전체를 조회한다. 없다면 빈 목록을 반환한다.
        log.info("최종 질문 인스턴스 ID 조회 완료: {}", targetInstanceId.orElse(null));
        return targetInstanceId
            .map(questionAssignmentRepository::findAllByInstanceId)
            .orElse(Collections.emptyList());
    }


    // XXXX 년 XX 월의 질문 세트 조회
    @Transactional(readOnly = true)
    public QuestionResponse findMonthlyQuestions(Family family, int year, int month) {
        // 해당 월의 모든 질문 인스턴스 조회
        YearMonth yearMonth = YearMonth.of(year, month);
        List<QuestionInstance> instances = questionInstanceRepository.findSentQuestionsByFamilyIdAndDateTimeRange(
            family.getId(), yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(LocalTime.MAX));
        if (instances.isEmpty()) return QuestionResponse.builder().questionDetails(List.of()).build();

        // 조회된 인스턴스들의 ID를 추출
        List<UUID> instanceIds = instances.stream()
            .map(QuestionInstance::getId)
            .toList();

        // 인스턴스 ID 목록을 사용해 관련된 모든 '질문 할당'을 한 번의 쿼리로 조회 (N+1 방지를 위해 Member 페치 조인)
        List<QuestionAssignment> questionAssignments = questionAssignmentRepository.findByInstanceIdsWithMembersAndQuestionInstance(instanceIds);

        return QuestionResponse.builder()
            .questionDetails(questionAssignments.stream()
                .map(QuestionDetails::from)
                .toList())
            .build();
    }

    public void generateAndAssignQuestionForFamily(Family family) {
        if (!isNewQuestionNeeded(family)) { // 새로운 질문이 필요하지 않다면 종료
            log.info("가족 ID {}는 새로운 질문 생성이 필요하지 않습니다.", family.getId());
            return;
        }

        // 오늘의 주인공 선정
        Map<UUID, Integer> memberAssignedCounts = familyService.getMemberAssignedCounts(family);
        MemberAssignResponse memberAssignResponse = aiRequestService.requestTodayMember(family.getId(), memberAssignedCounts, 1);

        // 어떤 방식으로 질문을 생성할지 정책에 따라 결정
        // TODO : 꼬리질문이 1회차인지 알기위해 엔티티에 boolean 추가해야하나? + tryGenerateFollowUpQuestion 검증로직 구현해야함
        log.info("가족 ID {}의 새로운 질문 생성을 시작합니다.", family.getId());
        AiQuestionResponse response = tryGenerateFollowUpQuestion(family)   // 꼬리 질문 시도 (1회만)
            .or(() -> tryGenerateTemplateQuestion(family))                  // 템플릿 질문 시도
            .orElseGet(() -> generateGeneralQuestion(family));              // 모두 해당 없으면 일반 질문 생성


        // 응답 기반으로 Instance 및 Assignment 생성
        saveInstanceAndAssignments(response, family, memberAssignResponse);
    }

    private boolean isNewQuestionNeeded(Family family) {
        // 가장 최신 질문 인스턴스 조회
        List<QuestionAssignment> assignments = questionAssignmentRepository.findMostRecentUnansweredAssignmentId(family.getId(), LocalDateTime.now());
        log.info("가족 ID {}의 최신 질문 할당 조회 완료. 총 {}개", family.getId(), assignments.size());
        Optional<QuestionAssignment> latestAssignment = assignments.stream().findFirst();
        log.info("최신 질문 할당 ID 조회 완료: {}", latestAssignment.orElse(null));
        if (latestAssignment.isEmpty()) return true;

        // 모든 질문이 답변된 경우에만 새 질문이 필요
        return assignments.stream().allMatch(assignment -> assignment.getState() == AssignmentState.ANSWERED);
    }

    /**
     * (1순위) 꼬리 질문 생성을 시도합니다.
     * @return 성공 시 AiQuestionResponse, 대상이 없으면 빈 Optional
     */
    private Optional<AiQuestionResponse> tryGenerateFollowUpQuestion(Family family) {

        List<Answer> answers = answerRepository.findAllByMember_Family_IdOrderByCreatedAtDesc(family.getId());
        if (answers.isEmpty()) return Optional.empty();
        log.info("가족 ID {}의 최근 답변 조회 완료. 총 {}개", family.getId(), answers.size());

        Answer recentAnswer = answers.getFirst();
        String prevQuestion = recentAnswer.getQuestionAssignment().getQuestionInstance().getContent();
        String prevAnswer = recentAnswer.getContent().path("text").asText("내용 없음");

        log.info("가족 ID {}의 꼬리 질문 생성을 시도합니다. (이전 답변 ID: {})", family.getId(), recentAnswer.getId());

        // AI에게 이전 질문/답변을 알려주며 꼬리 질문을 요청하는 DTO 구성
        AiQuestionRequest request = AiQuestionRequest.forFollowUp(prevQuestion, prevAnswer);

        return Optional.of(aiRequestService.requestQuestionGeneration(request));
    }

    /**
     * (2순위) 템플릿 기반 질문 생성을 시도합니다.
     * @return 성공 시 AiQuestionResponse, 대상이 없으면 빈 Optional
     */
    private Optional<AiQuestionResponse> tryGenerateTemplateQuestion(Family family) {
        // 해당 가족이 최근 N일 내에 사용하지 않은 템플릿 중 하나를 무작위로 조회
        // QuestionTemplate을 반환하는 쿼리는 QuestionTemplateRepository에 속한다 : SRP - 책임 분리 원칙
        List<QuestionTemplate> templates = questionTemplateRepository
            .findUnusedTemplatesRecentlyByFamily(family.getId(), LocalDateTime.now().minusMonths(2L));
        if (templates.isEmpty()) return Optional.empty();

        // DB 부하 없이, 애플리케이션 메모리에서 무작위 선택
        Collections.shuffle(templates);
        QuestionTemplate template = templates.getFirst();
        log.info("가족 ID {}의 템플릿 질문 생성을 시도합니다. (템플릿 ID: {})", family.getId(), template.getId());

        // 템플릿 내용을 기반으로 질문을 생성하도록 AI에 요청
        AiQuestionRequest request = AiQuestionRequest.fromTemplate(template);
        AiQuestionResponse response = aiRequestService.requestQuestionGeneration(request);

        // 생성된 질문 인스턴스에 어떤 템플릿을 사용했는지 기록
        AiQuestionResponse responseWithTemplate = response.toBuilder()
            .usedTemplateId(template.getId())
            .build();
        return Optional.of(responseWithTemplate);
    }

    /**
     * (3순위) 일반적인 AI 질문을 생성합니다.
     * @return 항상 AiQuestionResponse 반환
     */
    private AiQuestionResponse generateGeneralQuestion(Family family) {
        log.info("가족 ID {}의 일반 질문을 생성합니다.", family.getId());
        AiQuestionRequest request = AiQuestionRequest.defaultRequest(); // 기본 프롬프트 사용
        return aiRequestService.requestQuestionGeneration(request);
    }

    /**
     * 전달받은 AI 응답과 주인공 정보를 바탕으로 QuestionInstance와 QuestionAssignment를 생성하고 저장합니다.
     */
    @Transactional
    public void saveInstanceAndAssignments(AiQuestionResponse response, Family family, MemberAssignResponse memberAssignResponse) {
        QuestionInstance questionInstance = QuestionInstance.generateByAI(response, family);

        // 만약 템플릿 질문이었다면, 사용된 템플릿 정보 연결
        if (response.getUsedTemplateId() != null) {
            QuestionTemplate usedTemplate = questionTemplateRepository.findById(response.getUsedTemplateId()).orElse(null);
            questionInstance.setTemplate(usedTemplate);
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
        log.info("삭제한 assignments : {}", questionAssignmentRepository.deleteAllByFamily(family));
        log.info("삭제한 instances : {}", questionInstanceRepository.deleteAllByFamily(family));
    }
}