package com.onsikku.onsikku_back.domain.answer.service;


import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.domain.Comment;
import com.onsikku.onsikku_back.domain.answer.dto.CommentRequest;
import com.onsikku.onsikku_back.domain.answer.dto.CommentResponse;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.answer.repository.CommentRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.notification.event.InteractionEvent;
import com.onsikku.onsikku_back.domain.notification.event.NotificationType;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
  private final CommentRepository commentRepository;
  private final AnswerRepository answerRepository;
  private final MemberRepository memberRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public CommentResponse createComment(CommentRequest request, Member member) {
    // 답변 존재 여부 확인
    Answer answer = answerRepository.findById(request.answerId())
        .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));
    // 가족 구성원 확인
    if (!answer.getFamily().getId().equals(member.getFamily().getId())) {
      throw new BaseException(BaseResponseStatus.INVALID_FAMILY_MEMBER);
    }
    Comment comment = Comment.createComment(answer, member, request.content());

    // 부모 댓글 존재 여부 확인
    if (request.parentCommentId() != null) {
      validateParentAndSetParent(request, comment);
    }

    commentRepository.save(comment);
    // 요약 만들기 (10자)
    String summary = request.content().length() > 10
        ? request.content().substring(0, 10)
        : request.content();

    for (Member familyMember : memberRepository.findAllByFamily_Id(member.getFamily().getId())) {
      if (!familyMember.getId().equals(member.getId()) && familyMember.isAlarmEnabled()) { // 주인공 본인에겐 알림 X + 알림 설정 시에만 전송
        eventPublisher.publishEvent(
            new InteractionEvent(
                familyMember.getId(),     // 알림 받는 사람
                NotificationType.COMMENT_ADDED,
                List.of(member.getNickname(), summary),   // 댓글은 댓글 작성자 닉네임과 요약 필요
                answer.getMemberQuestion().getId()));
      }
    }
    return CommentResponse.builder()
        .comment(comment)
        .build();
  }

  @Transactional
  public CommentResponse updateComment(CommentRequest request, Member member) {
    Comment comment = commentRepository.findByIdWithMemberAndParentAndParentMember(request.commentId())
        .orElseThrow(() -> new BaseException(BaseResponseStatus.COMMENT_NOT_FOUND));
    if (!comment.getMember().getId().equals(member.getId())) {
      throw new BaseException(BaseResponseStatus.ACCESS_DENIED_FOR_RESOURCE);
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
      throw new BaseException(BaseResponseStatus.ACCESS_DENIED_FOR_RESOURCE);
    }
    // 자식 댓글은 자동으로 null 처리됨 (OnDeleteAction.SET_NULL)
    commentRepository.delete(comment);
  }

  private void validateParentAndSetParent(CommentRequest request, Comment comment) {
    Comment parentComment = commentRepository.findByIdWithMember(request.parentCommentId())
        .orElseThrow(() -> new BaseException(BaseResponseStatus.COMMENT_NOT_FOUND));
    if(parentComment.getParentComment() != null) {
      throw new BaseException(BaseResponseStatus.CANNOT_NESTED_COMMENT);
    }
    // 부모 댓글이 같은 답변에 속하는지 확인
    if (!parentComment.getAnswer().getId().equals(request.answerId())) {
      throw new BaseException(BaseResponseStatus.INVALID_PARENT_COMMENT);
    }
    comment.setParentComment(parentComment);
  }
}
