package com.onsikku.onsikku_back.domain.member.domain;


import com.onsikku.onsikku_back.global.entity.BaseEntity;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Family extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String familyName;

    @Column(nullable = true, unique = true, length = 8)
    private String invitationCode;

    @Column(nullable = false)
    private boolean isFamilyInviteEnabled;

    @Column(nullable = false)
    private LocalDateTime lastAiQuestionDate;

    public static Family registerNewFamily(String familyName, String invitationCode) {
        return Family.builder()
                .familyName(familyName)
                .invitationCode(invitationCode)
                .isFamilyInviteEnabled(true)
                .lastAiQuestionDate(LocalDateTime.now())
                .build();
    }

    public void changeInvitationCode(String newCode) {
        if (!newCode.matches("^[A-Z0-9]{8}$")) {
            throw new BaseException(BaseResponseStatus.INVALID_GENERATED_INVITATION_CODE);
        }
        this.invitationCode = newCode;
    }

    public void changeFamilyInviteEnabled(boolean isEnabled) {
        this.isFamilyInviteEnabled = isEnabled;
    }

    public void deleteInvitationCode() {
        this.invitationCode = null;
    }

    /**
     * AI 질문 생성 가능 여부 확인 (n일 경과 여부)
     */
    public boolean isEligibleForAiQuestion(int days) {
        return lastAiQuestionDate.isBefore(LocalDateTime.now().minusDays(days));
    }

    /**
     * AI 질문 생성 시간 업데이트
     */
    public void updateLastAiQuestionDate() {
        this.lastAiQuestionDate = LocalDateTime.now();
    }
}