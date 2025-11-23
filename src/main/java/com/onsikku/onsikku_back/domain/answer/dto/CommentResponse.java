package com.onsikku.onsikku_back.domain.answer.dto;


import com.onsikku.onsikku_back.domain.answer.domain.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponse {
  private Comment comment;
}
