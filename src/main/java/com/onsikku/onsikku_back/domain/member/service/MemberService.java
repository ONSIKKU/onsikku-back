package com.onsikku.onsikku_back.domain.member.service;

import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.answer.repository.CommentRepository;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.dto.MypageRequest;
import com.onsikku.onsikku_back.domain.member.dto.MypageResponse;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.FamilyRepository;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.member.util.InvitationCodeGenerator;
import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import com.onsikku.onsikku_back.domain.question.repository.QuestionAssignmentRepository;
import com.onsikku.onsikku_back.domain.question.repository.QuestionInstanceRepository;
import com.onsikku.onsikku_back.global.auth.service.AuthService;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.jwt.JwtProvider;
import com.onsikku.onsikku_back.global.redis.RedisService;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.onsikku.onsikku_back.global.jwt.TokenConstants.RT_KEY_PREFIX;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final FamilyRepository familyRepository;
    private final AnswerRepository answerRepository;
    private final QuestionAssignmentRepository questionAssignmentRepository;
    private final CommentRepository commentRepository;
    private final InvitationCodeGenerator invitationCodeGenerator;

    public MypageResponse getMemberById(UUID memberId) {
        return MypageResponse.from(
            memberRepository.findMemberWithFamily(memberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND))
        );
    }

    @Transactional
    public MypageResponse updateMemberById(MypageRequest req, UUID memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        // 부분 업데이트 (JsonNullable 사용 가정)
        req.profileImageUrl().ifPresent(member::changeProfileImageUrl);
        req.familyRole().ifPresent(member::changeFamilyRole);
        req.birthDate().ifPresent(member::changeBirthDate);
        req.gender().ifPresent(member::changeGender);
        req.isAlarmEnabled().ifPresent(member::changeAlarmEnabled);

        // 초대코드 재발급
        if (req.regenerateFamilyInvitationCode().orElse(false)) {
            Family family = member.getFamily();
            regenerateUniqueInvitationCode(family); // 별도 메서드로 추출
        }

        return MypageResponse.from(member);
    }

    @Transactional
    public void deleteMember(Member member) {
        // TODO : 회원이 생성한 답변 softDelete 처리
        answerRepository.deleteByMember(member);
        questionAssignmentRepository.deleteByMember(member);
        commentRepository.deleteByMember(member);
        // TODO : 회원 삭제 softDelete 처리
        memberRepository.deleteById(member.getId());
        log.info("회원 삭제 완료");
    }

    private void regenerateUniqueInvitationCode(Family family) {
        final int MAX_TRY = 10;
        for (int i = 0; i < MAX_TRY; i++) {
            String candidate = invitationCodeGenerator.generate();
            try {
                if (!familyRepository.existsByInvitationCode(candidate)) {
                    family.changeInvitationCode(candidate);
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