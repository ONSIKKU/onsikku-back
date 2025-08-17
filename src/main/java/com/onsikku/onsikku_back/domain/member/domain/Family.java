package com.onsikku.onsikku_back.domain.member.domain;


import com.onsikku.onsikku_back.global.entity.BaseEntity;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false, unique = true, length = 8)
    private String invitationCode;

    @Enumerated(EnumType.STRING)
    private GrandParentType grandparentType;

    public void changeInvitationCode(String newCode) {
        if (!newCode.matches("^[A-Z0-9]{8}$")) {
            throw new BaseException(BaseResponseStatus.INVALID_GENERATED_INVITATION_CODE);
        }
        this.invitationCode = newCode;
    }
}