package com.onsikku.onsikku_back.domain.member.service;

import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.answer.repository.CommentRepository;
import com.onsikku.onsikku_back.domain.answer.repository.ReactionRepository;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.dto.MypageRequest;
import com.onsikku.onsikku_back.domain.member.dto.MypageResponse;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.FamilyRepository;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.member.util.InvitationCodeGenerator;
import com.onsikku.onsikku_back.domain.question.service.QuestionService;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final FamilyRepository familyRepository;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;
    private final InvitationCodeGenerator invitationCodeGenerator;
    private final QuestionService questionService;
    private final ReactionRepository reactionRepository;

    public MypageResponse getMemberByMember(Member member) {
        return MypageResponse.from(
            memberRepository.findMemberWithFamily(member.getId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND)),
            memberRepository.findAllByFamily_Id(member.getFamily().getId())
        );
    }

    @Transactional
    public MypageResponse updateMember(MypageRequest req, UUID memberId) {
        Member member = memberRepository.findMemberWithFamily(memberId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        // 부분 업데이트 (JsonNullable 사용 가정)
        req.nickname().ifPresent(member::changeNickname);
        req.profileImageUrl().ifPresent(member::changeProfileImageUrl);
        req.familyRole().ifPresent(member::changeFamilyRole);
        req.birthDate().ifPresent(member::changeBirthDate);
        req.isAlarmEnabled().ifPresent(member::changeAlarmEnabled);

        // 초대코드 재발급
        // false -> true 로 변경되는 경우에만 재발급 (true -> true 인 경우는 자동 무시)
        Family family = member.getFamily();
        if (req.isFamilyInviteEnabled().orElse(false) && !member.getFamily().isFamilyInviteEnabled()) {
            log.info("Family Invite Enabled");
            regenerateUniqueInvitationCode(family); // 별도 메서드로 추출
        }
        // true -> false 인 경우 가족의 초대 설정 변경
        // false -> false 인 경우 변화 없음
        else if (!req.isFamilyInviteEnabled().orElse(true) && member.getFamily().isFamilyInviteEnabled()) {
            log.info("Family Invite Disabled");
            family.changeFamilyInviteEnabled(false);
            family.deleteInvitationCode();
        }
        return MypageResponse.from(member, memberRepository.findAllByFamily_Id(family.getId()));
    }

    @Transactional
    public void deleteMember(Member member) {
        // TODO : 회원이 생성한 답변 softDelete 처리
        // TODO : 회원이 생성한 댓글 성능 고려한 쿼리로 수정
        List<Answer> answers = answerRepository.findAllByMember_Id(member.getId());
        log.info("회원이 생성한 답변 조회 완료: {}건", answers.size());
        log.info("회원이 생성한 댓글 삭제 완료 : {} 개", commentRepository.deleteAllByMember(member));
        log.info("회원이 생성한 반응 삭제 완료 : {} 개", reactionRepository.deleteAllByMember(member));
        log.info("회원이 생성한 답변 삭제 완료 : {} 개", answerRepository.deleteAllByMember(member));
        // TODO : 회원 삭제 softDelete 처리
        UUID familyId = member.getFamily().getId();
        memberRepository.deleteById(member.getId());
        log.info("회원 삭제 완료");
        if(memberRepository.findAllByFamily_Id(familyId).isEmpty()) {
            log.info("가족에 속한 회원이 없어 가족 데이터 삭제를 진행합니다.");
            questionService.deleteFamilyData(familyId);
            familyRepository.deleteById(familyId);
            log.info("회원의 가족 삭제 완료");
        }
    }

    private void regenerateUniqueInvitationCode(Family family) {
        final int MAX_TRY = 10;
        for (int i = 0; i < MAX_TRY; i++) {
            String candidate = invitationCodeGenerator.generate();
            try {
                if (!familyRepository.existsByInvitationCode(candidate)) {
                    log.info("가족 초대 코드 생성 성공, unique 초대 코드 생성 시도 횟수: {}회", i + 1);
                    family.changeInvitationCode(candidate);
                    family.changeFamilyInviteEnabled(true);
                    return;
                }
            } catch (DataIntegrityViolationException e) {
                // 충돌 발생 → 다음 루프로 재시도
                log.warn("가족 초대 코드 생성 후 flush 중 충돌 발생: {}", e.getMessage());
                log.warn("재시도: " + (i + 1) + "회");
            }
        }
        throw new BaseException(BaseResponseStatus.INVITATION_CODE_GENERATION_FAILED);
    }
}