package com.onsikku.onsikku_back.domain.answer.service;

import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.domain.Reaction;
import com.onsikku.onsikku_back.domain.answer.domain.ReactionType;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.answer.repository.ReactionRepository;
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
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final AnswerRepository answerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MemberRepository memberRepository;

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
        for (Member familyMember : memberRepository.findAllByFamily_Id(member.getFamily().getId())) {
            if (!familyMember.getId().equals(member.getId()) && familyMember.isAlarmEnabled()) { // 주인공 본인에겐 알림 X + 알림 설정 시에만 전송
                eventPublisher.publishEvent(
                    new InteractionEvent(
                        familyMember.getId(),       // 알림 받는 사람
                        NotificationType.REACTION_ADDED,
                        List.of(member.getNickname()),
                        answerId)
                );
            }
        }
    }

    @Transactional
    public void deleteReaction(UUID answerId, Member member) {
        Reaction reaction = reactionRepository.findByAnswer_Id(answerId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.REACTION_NOT_FOUND));

        // 본인 확인
        if (!reaction.getMember().getId().equals(member.getId())) {
            throw new BaseException(BaseResponseStatus.ACCESS_DENIED_FOR_RESOURCE);
        }

        reactionRepository.delete(reaction);
    }
}