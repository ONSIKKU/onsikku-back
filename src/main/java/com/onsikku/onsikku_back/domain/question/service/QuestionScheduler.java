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

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionScheduler {

  private final QuestionService questionService;
  private final FamilyRepository familyRepository; // Family 조회를 위해 주입
  private static final int PAGE_SIZE = 10; // 한 번에 처리할 가족의 수

  /**
   * 매일 밤 10시에 모든 가족을 대상으로 질문을 생성하고 할당합니다.
   */
  @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
  public void createDailyQuestions() {
    log.info("[BATCH] Daily question creation job started.");

    Pageable pageable = PageRequest.of(0, PAGE_SIZE);
    Page<Family> familyPage;

    do {
      // DB에서 PAGE_SIZE 만큼의 가족만 조회
      familyPage = familyRepository.findAll(pageable);

      // 조회된 가족들에 대해 질문 생성 로직 실행
      for (Family family : familyPage.getContent()) {
        try {
          questionService.generateAndAssignQuestionForFamily(family);
        } catch (Exception e) {
          // 한 가족 처리 중 에러가 나도 로그만 남기고 계속 진행
          log.error("[BATCH] Failed to process family ID: {}. Error: {}", family.getId(), e.getMessage());
        }
      }
      pageable = pageable.next(); // 다음 페이지로 이동
    } while (familyPage.hasNext()); // 다음 페이지가 있으면 루프 계속

    log.info("[BATCH] Daily question creation job finished.");
  }
}