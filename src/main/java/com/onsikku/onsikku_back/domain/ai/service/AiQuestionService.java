package com.onsikku.onsikku_back.domain.ai.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onsikku.onsikku_back.domain.ai.entity.*;
import com.onsikku.onsikku_back.domain.ai.model.*;
import com.onsikku.onsikku_back.domain.ai.repository.*;
import com.onsikku.onsikku_back.domain.member.domain.*;
import com.onsikku.onsikku_back.domain.ai.entity.GeneratedBy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.*;
import java.util.*;


@Service
@RequiredArgsConstructor
public class AiQuestionService {
  private final QuestionTemplateRepository questionTemplateRepository;
  private final QuestionInstanceRepository questionInstanceRepository;
  private final QuestionAssignmentRepository questionAssignmentRepository;
  private final AnswerRepository answerRepository;
  private final ObjectMapper objectMapper;


  /**
   * Create or get the daily instance (family + plannedDate unique)
   */
  @Transactional
  public QuestionInstance upsertDailyInstance(Family family, LocalDate plannedDate, String finalContent,
                                              GeneratedBy generatedBy, String model, JsonNode genParams,
                                              String prompt, JsonNode genMeta, Float confidence,
                                              Member subject, QuestionTemplate sourceTemplate) {
    return questionInstanceRepository.findByFamilyAndPlannedDate(family, plannedDate)
        .orElseGet(() -> {
          QuestionInstance qi = QuestionInstance.builder()
              .family(family)
              .plannedDate(plannedDate)
              .content(finalContent)
              .status(InstanceStatus.DRAFT)
              .generatedBy(generatedBy)
              .generationModel(model)
              .generationParameters(genParams)
              .generationPrompt(prompt)
              .generationMetadata(genMeta)
              .generationConfidence(confidence)
              .subject(subject)
              .template(sourceTemplate)
              .generatedAt(OffsetDateTime.now())
              .build();
          return questionInstanceRepository.save(qi);
        });
  }
  /** Assign to recipients (idempotent via unique constraint) */
  @Transactional
  public List<QuestionAssignment> assignToRecipients(QuestionInstance instance, List<Member> recipients, OffsetDateTime due) {
    List<QuestionAssignment> result = new ArrayList<>();
    for (Member m : recipients) {
      QuestionAssignment qa = QuestionAssignment.builder()
          .instance(instance)
          .recipient(m)
          .dueAt(due)
          .state(AssignmentState.PENDING)
          .reminderCount(0)
          .build();
      try { result.add(questionAssignmentRepository.save(qa)); } catch (Exception ignored) { /* unique -> skip */ }
    }
    return result;
  }
  /** Submit answer with validation mirrored from entity prePersist */
  @Transactional
  public Answer submitAnswer(QuestionAssignment assignment, Member author, AnswerType type, JsonNode content) {
    Answer answer = Answer.builder()
        .assignment(assignment)
        .author(author)
        .answerType(type)
        .content(content)
        .build();
    Answer saved = answerRepository.save(answer);
    assignment.setState(AssignmentState.ANSWERED);
    assignment.setAnsweredAt(OffsetDateTime.now());
    return saved;
  }
}