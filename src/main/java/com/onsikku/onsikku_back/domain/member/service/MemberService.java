package com.onsikku.onsikku_back.domain.member.service;

import com.onsikku.onsikku_back.domain.ai.dto.request.AiQuestionRequest;
import com.onsikku.onsikku_back.domain.ai.service.AiRequestService;
import com.onsikku.onsikku_back.domain.answer.repository.ReactionRepository;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.WithdrawalReason;
import com.onsikku.onsikku_back.domain.member.dto.DeleteMemberRequest;
import com.onsikku.onsikku_back.domain.member.dto.MypageRequest;
import com.onsikku.onsikku_back.domain.member.dto.MypageResponse;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.BlockRepository;
import com.onsikku.onsikku_back.domain.member.repository.FamilyRepository;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.member.util.InvitationCodeGenerator;
import com.onsikku.onsikku_back.domain.notification.repository.FcmTokenRepository;
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
    private final InvitationCodeGenerator invitationCodeGenerator;
    private final ReactionRepository reactionRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final AiRequestService aiRequestService;
    private final BlockRepository blockRepository;

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
        Member managedMember = memberRepository.findMemberWithFamily(member.getId())
            .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        if (managedMember.isWithdrawn()) {
            log.info("이미 탈퇴 처리된 회원입니다. memberId={}", managedMember.getId());
            return;
        }

        log.info("회원이 생성한 반응 삭제 완료 : {} 개", reactionRepository.deleteAllByMember(managedMember));
        UUID familyId = managedMember.getFamily().getId();
        log.info("회원이 로그인한 모든 기기의 fcm 토큰 삭제 완료 : {} 개", fcmTokenRepository.deleteAllByMember_Id(managedMember.getId()));
        blockRepository.deleteByMemberId(managedMember.getId());       // 회원이 차단한 내역과, 회원을 차단한 내역 모두 삭제
        log.info("회원 관련 AI 데이터 삭제 완료 : {} 개", aiRequestService.requestMemberDataDeletion(AiQuestionRequest.builder().memberId(managedMember.getId()).build()));

        List<WithdrawalReason> reasons = List.of(WithdrawalReason.OTHER);
            //request == null ? null : request.reasons();
        String reasonDetail = "임시 사유";
            //request == null ? null : request.reasonDetail();
        managedMember.withdraw("withdrawn_" + managedMember.getId(), reasons, reasonDetail);
        log.info("회원 탈퇴 익명화 처리 완료");

        if (memberRepository.countByFamily_IdAndWithdrawnAtIsNull(familyId) == 0) {
            log.info("가족 내 활성 회원이 없어 가족을 soft delete 처리합니다.");
            managedMember.getFamily().withdraw();
            log.info("가족 soft delete 처리 완료");
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
