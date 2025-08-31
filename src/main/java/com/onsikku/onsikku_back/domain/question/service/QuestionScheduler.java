package com.onsikku.onsikku_back.domain.question.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionScheduler {

  private final QuestionService questionService;

  /**
   * 매일 밤 10시에 모든 가족을 대상으로 질문을 생성하고 할당합니다.
   */
  @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul") // 한국 시간 기준 매일 밤 10시
  public void createDailyQuestions() {
    log.info("[BATCH] Daily question creation job started.");
    try {
      questionService.createQuestionForAllFamilies();
    } catch (Exception e) {
      log.error("[BATCH] Daily question creation job failed.", e);
    }
    log.info("[BATCH] Daily question creation job finished.");
  }
}