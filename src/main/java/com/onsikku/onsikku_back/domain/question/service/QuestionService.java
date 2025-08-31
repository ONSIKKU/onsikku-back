package com.onsikku.onsikku_back.domain.question.service;



import com.onsikku.onsikku_back.domain.ai.service.AiRequestService;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.repository.FamilyRepository;
import com.onsikku.onsikku_back.domain.member.service.MemberService;
import com.onsikku.onsikku_back.domain.question.domain.AssignmentState;
import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import com.onsikku.onsikku_back.domain.question.dto.QuestionRequest;
import com.onsikku.onsikku_back.domain.question.repository.QuestionAssignmentRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.repository.QuestionInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {
    private final QuestionAssignmentRepository questionAssignmentRepository;
    private final QuestionInstanceRepository questionInstanceRepository;
    private final FamilyRepository familyRepository;

    private final MemberService memberService;
    private final AiRequestService aiRequestService;


    /**
     * 특정 가족의 질문 세트를 조회하는 로직
     * 1. 가장 오래된 미답변 질문 INSTANCE 우선 조회
     * 2. 미답변 질문이 없다면, 가장 최신 질문 INSTANCE 조회
     * 3. 해당 INSTANCE ID가 포함된 모든 질문 ASSIGNMENT 조회
     * @param family 질문을 조회할 가족
     * @return 조회된 질문 세트 목록
     */
    public List<QuestionAssignment> findQuestions(Family family) {
        // 가장 오래된 미답변 질문의 ID를 먼저 찾는다 (LIMIT 1 적용)
        Optional<UUID> targetInstanceId = questionAssignmentRepository.findOldestUnansweredInstanceId(
                family.getId(), AssignmentState.ANSWERED, PageRequest.of(0, 1));

        if (targetInstanceId.isEmpty()) {
            // 미답변 질문 ID가 없다면, 가장 최신 질문의 ID를 찾는다 (LIMIT 1 적용)
            targetInstanceId = questionAssignmentRepository
                .findMostRecentInstanceId(family.getId(), LocalDate.now(), PageRequest.of(0, 1));
        }

        // 최종적으로 찾은 ID가 있다면, 해당 ID로 질문 세트 전체를 조회한다. 없다면 빈 목록을 반환한다.
        return targetInstanceId
            .map(questionAssignmentRepository::findAllByInstanceId)
            .orElse(Collections.emptyList());
    }

    // 질문 삭제
    public void deleteQuestion(QuestionRequest request) {
        questionAssignmentRepository.deleteById(request.id());
    }

    // 지난 질문 가져오기
    public List<QuestionAssignment> findMonthlyQuestions(Family family, int year, int month) {
        // 지난 달 1일부터 이번 달 말일까지의 질문을 모두 가져오기
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // TODO : instance 반환 시, Member 정보 같이 반환하는 방법 고민
        // 1. 해당 월의 모든 질문 인스턴스를 조회합니다. (N+1 방지를 위해 subject 페치 조인)
        List<QuestionInstance> instances = questionInstanceRepository.findByFamilyIdAndPlannedDateBetween(family.getId(), startDate, endDate);

        if (instances.isEmpty()) {
            // TODO: 실제 DTO 구조에 맞게 수정
            return List.of(); // new MonthlyRecordResponseDto(new MonthlySummaryDto(0,0,0), List.of());
        }

        List<UUID> instanceIds = instances.stream().map(QuestionInstance::getId).toList();

        // 2. 한 번의 쿼리로 모든 관련 데이터를 가져옵니다 (N+1 방지).
        // Map<UUID, List<QuestionAssignment>> assignmentsMap = fetchAssignmentsMap(instanceIds);
        // Map<UUID, List<ReactionDto>> reactionsMap = fetchReactionsMap(instanceIds);
        // MonthlySummaryDto summaryDto = recordQueryRepository.getSummary(familyId, startDate, endDate);

        // 3. 조회한 데이터를 가공하여 최종 응답 DTO를 조립합니다.
        // 이 부분은 실제 DTO 구조에 맞춰 `instances` 리스트를 루프 돌면서 DTO를 생성하는 로직이 들어갑니다.
        // List<QuestionRecordDto> questionDtos = instances.stream().map(instance -> {
        //     List<QuestionAssignment> assignments = assignmentsMap.getOrDefault(instance.getId(), List.of());
        //     String status = determineStatus(assignments); // 답변 완료 여부 계산
        //     List<ReactionDto> reactions = reactionsMap.getOrDefault(instance.getId(), List.of());
        //     // ... DTO 생성 로직 ...
        // }).toList();

        // return new MonthlyRecordResponseDto(summaryDto, questionDtos);
        return List.of(); // TODO : 실제 DTO 구조에 맞게 수정
    }


    /**
     * 특정 가족을 위한 질문 생성, 할당, 저장 로직
     * 스프링 스케줄러 전용 메서드입니다.
     * @param family 질문을 생성할 가족
     */
    @Transactional
    public void generateAndAssignQuestionForFamily(Family family) {
        log.info("Processing family: {}", family.getFamilyName());

        // 오늘의 주인공 선정
        List<Member> spotlights = selectSpotLights(family);

        // 3. AI 요청 파라미터 준비 및 LLM 호출
        // AiQuestionResponse response = aiLlmClient.generateQuestion(...);

        // 4. AI 응답 기반으로 QuestionInstance 생성 및 저장
        List<QuestionAssignment> assignments = new ArrayList<>();
         for (Member member : spotlights) {
             QuestionAssignment assignment = createQuestionInstance();
             assignments.add(assignment);
         }
        // TODO : QuestionInstance 저장
        // TODO : template 어떻게 할건지 고민
        //questionInstanceRepository.save(instance);
        questionAssignmentRepository.saveAll(assignments);
    }

    private QuestionAssignment createQuestionInstance() {
        // questionInstance 생성 후, 할당 할 멤버에게 questionAssignment 생성
        return QuestionAssignment.builder()
            .build();
    }

    private List<Member> selectSpotLights(Family family) {
        //List<Member> members = memberService.findMembersByFamily(family);
        return null;
        // TODO : 오늘의 주인공 선정 로직 구현 - AI 활용 or 백엔드 랜덤 설정
        // TODO : AI가 반환할건 어떤건지 필요
    }
}