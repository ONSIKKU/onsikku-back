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
  private final ObjectMapper om;


  @PostMapping("/instances")
  public UUID upsertInstance(@RequestParam UUID familyId,
                             @RequestParam String content,
                             @RequestParam String plannedDate) {
  // Fetch Family/Member from your existing services/repos (omitted here)
    Family family = new Family();
    family.setId(familyId);
    QuestionInstance questionInstance = aiQuestionService.upsertDailyInstance(
        family,
        LocalDate.parse(plannedDate),
        content,
        GeneratedBy.MANUAL,
        null,
        null,
        null,
        null,
        null,
        null,
        null);
    return questionInstance.getId();
  }
}