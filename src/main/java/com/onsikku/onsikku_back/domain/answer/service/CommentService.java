package com.onsikku.onsikku_back.domain.answer.service;


import com.onsikku.onsikku_back.domain.answer.domain.Comment;
import com.onsikku.onsikku_back.domain.answer.dto.CommentRequest;
import com.onsikku.onsikku_back.domain.answer.dto.CommentResponse;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.answer.repository.CommentRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import com.onsikku.onsikku_back.domain.question.repository.MemberQuestionRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
  private final CommentRepository commentRepository;
  private final AnswerRepository answerRepository;
  private final MemberQuestionRepository memberQuestionRepository;

  @Transactional
  public CommentResponse createComment(CommentRequest request, Member member) {
    // 질문 인스턴스 존재 여부 확인
    QuestionInstance instance = memberQuestionRepository.findById(request.questionInstanceId())
        .orElseThrow(() -> new BaseException(BaseResponseStatus.QUESTION_NOT_FOUND));
    // 가족 구성원 확인
    if (!instance.getFamily().getId().equals(member.getFamily().getId())) {
      throw new BaseException(BaseResponseStatus.INVALID_FAMILY_MEMBER);
    }
    // 해당 질문 인스턴스에 답변이 존재하는지 확인
    if(answerRepository.findAllByQuestionInstanceId(request.questionInstanceId()).isEmpty()) {
      throw new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND);
    }
    Comment comment = Comment.builder()
        .content(request.content())
        .member(member)
        .build();
    log.info("Parent Comment ID: {}", request.parentCommentId());
    if (request.parentCommentId() != null) {
      // 부모 댓글 존재 여부 확인
      Comment parentComment = commentRepository.findByIdWithMember(request.parentCommentId())
          .orElseThrow(() -> new BaseException(BaseResponseStatus.COMMENT_NOT_FOUND));
      if(parentComment.getParentComment() != null) {
        throw new BaseException(BaseResponseStatus.CANNOT_NESTED_COMMENT);
      }
      // 부모 댓글이 같은 질문 인스턴스에 속하는지 확인
      if (!parentComment.getQuestionInstance().getId().equals(request.questionInstanceId())) {
        throw new BaseException(BaseResponseStatus.INVALID_PARENT_COMMENT);
      }
      comment.setParentComment(parentComment);
    }
    commentRepository.save(comment);
    return CommentResponse.builder()
        .comment(comment)
        .build();
  }

  @Transactional
  public CommentResponse updateComment(CommentRequest request, Member member) {
    Comment comment = commentRepository.findByIdWithMemberAndParentAndParentMember(request.commentId())
        .orElseThrow(() -> new BaseException(BaseResponseStatus.COMMENT_NOT_FOUND));
    if (!comment.getMember().getId().equals(member.getId())) {
      throw new BaseException(BaseResponseStatus.CANNOT_MODIFY_OTHER_COMMENT);
    }
    comment.updateContent(request.content());
    comment.setMember(member);
    return CommentResponse.builder()
        .comment(comment)
        .build();
  }
  @Transactional
  public void deleteComment(UUID commentId, Member member) {
    //TODO : soft delete로 변경 고려
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.COMMENT_NOT_FOUND));
    if (!comment.getMember().getId().equals(member.getId())) {
      throw new BaseException(BaseResponseStatus.CANNOT_MODIFY_OTHER_COMMENT);
    }
    // 자식 댓글은 자동으로 null 처리됨 (OnDeleteAction.SET_NULL)
    commentRepository.delete(comment);
  }
}
