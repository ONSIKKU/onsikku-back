package com.onsikku.onsikku_back.domain.question.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class QuestionResponse {
  private List<QuestionDetails> questionDetails;
}
