package com.onsikku.onsikku_back.domain.ai.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.onsikku.onsikku_back.domain.ai.service.AiQuestionService;
import com.onsikku.onsikku_back.domain.member.domain.*;
import com.onsikku.onsikku_back.domain.question.domain.GeneratedBy;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.time.*;
import java.util.*;


@RestController
@RequestMapping("/api/ai/questions")
@RequiredArgsConstructor
public class AiQuestionController {
  private final AiQuestionService aiQuestionService;


  // 분석에 필요한 가족별 질문 및 답변 반환 API??
  @PostMapping("/instances")
  public UUID upsertInstance(@RequestParam UUID familyId,
                             @RequestParam String content,
                             @RequestParam String plannedDate) {

    return null;
  }

}