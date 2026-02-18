package com.onsikku.onsikku_back.domain.answer.service;

import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.domain.Reaction;
import com.onsikku.onsikku_back.domain.answer.domain.ReactionType;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.answer.repository.ReactionRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.notification.event.NotificationType;
import com.onsikku.onsikku_back.domain.notification.service.NotificationService;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final AnswerRepository answerRepository;
    private final NotificationService notificationService;

    @Transactional
    public void createReaction(UUID answerId, ReactionType type, Member member) {
        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));

        // 가족 구성원 확인
        if (!answer.getFamily().getId().equals(member.getFamily().getId())) {
            throw new BaseException(BaseResponseStatus.INVALID_FAMILY_MEMBER);
        }

        // 이미 반응이 있는지 확인
        if (reactionRepository.existsByAnswer_IdAndMember_Id(answer.getId(), member.getId())) {
            throw new BaseException(BaseResponseStatus.REACTION_ALREADY_EXISTS);
        }

        reactionRepository.save(Reaction.createReaction(answer, member, type));
        notificationService.publishEvent(member, NotificationType.REACTION_ADDED, List.of(member.getNickname()), answer.getMemberQuestion().getId());
    }

    @Transactional
    public void deleteReaction(UUID answerId, Member member) {
        reactionRepository.deleteByAnswer_IdAndMember_Id(answerId, member.getId());
    }
}