package com.onsikku.onsikku_back.domain.question.service;

import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.repository.FamilyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionScheduler {

  private final QuestionService questionService;
  private final FamilyRepository familyRepository; // Family 조회를 위해 주입
  private final QuestionCycleService questionCycleService;
  private static final int PAGE_SIZE = 10; // 한 번에 처리할 가족의 수

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