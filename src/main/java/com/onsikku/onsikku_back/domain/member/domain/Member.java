package com.onsikku.onsikku_back.domain.member.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.onsikku.onsikku_back.global.auth.dto.KakaoMemberInfo;
import com.onsikku.onsikku_back.global.auth.dto.KakaoSignupRequest;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Setter
    @JsonIgnore
    private Family family;

    @Column(nullable = false, unique = true)
    @JsonIgnore             // 보안상 카카오 ID는 외부에 노출하지 않음
    private String kakaoId;

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private Role role;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private FamilyRole familyRole;

    @Column(nullable = false, length = 20)
    private String nickname;

    private String profileImageUrl;

    private boolean isAlarmEnabled;

    public void changeBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void changeFamilyRole(FamilyRole familyRole) {
        this.familyRole = familyRole;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeProfileImageUrl(String url) {
        this.profileImageUrl = url;
    }

    public void changeAlarmEnabled(boolean enabled) {
        this.isAlarmEnabled = enabled;
    }

    public static Member from(KakaoMemberInfo memberInfo, KakaoSignupRequest request, Family family) {
        return Member.builder()
            .kakaoId(memberInfo.kakaoId())
            .familyRole(request.familyRole())
            .nickname(request.nickname())
            .birthDate(request.birthDate())
            .profileImageUrl(request.profileImageUrl())
            .family(family)
            .role(Role.MEMBER)
            .isAlarmEnabled(true)
            .build();
    }
}
