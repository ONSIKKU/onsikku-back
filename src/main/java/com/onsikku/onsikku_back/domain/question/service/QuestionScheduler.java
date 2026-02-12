package com.onsikku.onsikku_back.domain.question.service;

import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.FamilyRepository;
import com.onsikku.onsikku_back.domain.notification.event.DailyQuestionEvent;
import com.onsikku.onsikku_back.domain.question.dto.QuestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionScheduler {

  private final QuestionService questionService;
  private final FamilyRepository familyRepository; // Family 조회를 위해 주입
  private final QuestionCycleService questionCycleService;
  private static final int PAGE_SIZE = 10; // 한 번에 처리할 가족의 수
  private final ApplicationEventPublisher eventPublisher;

  /**
   * 매일 새벽 4시 30분에 모든 가족을 대상으로 질문을 생성하고 할당합니다.
   * @Scheduled 가 붙으면, 웹 요청 쓰레드가 아닌 별도의 스케줄러 쓰레드에 할당됩니다.
   * 따라서 비동기 처리는 필요하지 않습니다.
   */
  @Scheduled(cron = "0 30 4 * * *", zone = "Asia/Seoul")
  public void createDailyQuestions() {
    log.info("[BATCH] Daily question creation job started at 04:30.");

    int pageNumber = 0;
    Page<Family> familyPage;
    // 질문 전송 시간을 매일 밤 10시로 설정
    LocalDateTime questionSendTime = LocalDateTime.now().withHour(22).withMinute(0).withSecond(0).withNano(0);

    do {
      Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE);
      familyPage = familyRepository.findAll(pageable);

      log.info("[BATCH] Processing page: {}, Families: {}", pageNumber, familyPage.getNumberOfElements());

      for (Family family : familyPage.getContent()) {
        try {
          // 각 가족별 사이클 확인 및 질문 할당 실행
          questionCycleService.getOrGenerateCycleAndAssignQuestionForFamily(family, questionSendTime);
        } catch (Exception e) {
          // 특정 가족 실패 시 해당 가족만 건너뛰고 계속 진행
          log.error("[BATCH] Error processing family {}: {}", family.getId(), e.getMessage());
        }
      }
      pageNumber++;
    } while (familyPage.hasNext());

    log.info("[BATCH] Daily question creation job finished.");
  }

  /**
   * [2] 알림 발송 스케줄러 (밤 10:00)
   * - 4시 30분에 만들어둔 데이터를 바탕으로, 실제 푸시 알림을 보냅니다.
   * - cron 설정을 "0 0 22 * * *"로 하면 밤 10시에 돕니다. (오전 10시면 "0 0 10 ...")
   */
  @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
  public void sendDailyQuestionNotifications() {
    log.info("[PUSH] 오늘의 질문 알림 발송 시작 (22:00)");

    int pageNumber = 0;
    Page<Family> familyPage;

    do {
      familyPage = familyRepository.findAll(PageRequest.of(pageNumber, PAGE_SIZE));

      for (Family family : familyPage.getContent()) {
        try {
          // 이 가족의 '오늘의 주인공'과 '오늘의 질문 ID'를 조회
          QuestionResponse questionResponse = questionService.getTodayMemberQuestionWithFamilyId(family.getId());

          // 가족 구성원들에게 알림 이벤트 발행
          for (Member familyMember : questionResponse.getFamilyMembers()) {
            boolean isTodayMember = familyMember.getId().equals(questionResponse.getQuestionDetails().getMember().getId());
            // Event 발행 -> 리스너가 받아서 FCM 쏨
            eventPublisher.publishEvent(
                new DailyQuestionEvent(
                    familyMember.getId(),         // 알림 받는 사람
                    questionResponse.getQuestionDetails().getMemberQuestionId(),
                    isTodayMember,
                    questionResponse.getQuestionDetails().getMember().getNickname())    // 주인공 닉네임
            );
          }

        } catch (Exception e) {
          log.error("가족 ID {} 알림 발송 실패: {}", family.getId(), e.getMessage());
        }
      }
      pageNumber++;
    } while (familyPage.hasNext());

    log.info("[PUSH] 오늘의 질문 알림 발송 완료");
  }

  /**
   * 매일 새벽 1시에 미답변 질문을 리마인드하거나 파기 처리합니다.
   */
  @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul") // 새벽 1시 실행
  public void manageUnansweredQuestions() {
    log.info("[BATCH] Unanswered question management job started.");
    try {
      int processedCount = questionService.remindOrExpireAssignments();
      log.info("[BATCH] Unanswered question management job finished. Total assignments processed: {}", processedCount);
    } catch (Exception e) {
      log.error("[BATCH] Failed to run unanswered question management job.", e);
    }
  }


}