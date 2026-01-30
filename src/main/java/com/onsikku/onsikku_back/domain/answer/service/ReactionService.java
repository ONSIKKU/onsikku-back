package com.onsikku.onsikku_back.domain.answer.service;

import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.domain.Reaction;
import com.onsikku.onsikku_back.domain.answer.domain.ReactionType;
import com.onsikku.onsikku_back.domain.answer.dto.ReactionRequest;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.answer.repository.ReactionRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
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
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final AnswerRepository answerRepository;

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