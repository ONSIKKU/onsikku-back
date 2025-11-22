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
    @JsonIgnore
    private String kakaoId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private FamilyRole familyRole;

    private String profileImageUrl;

    private boolean isAlarmEnabled;

    public void changeGender(Gender gender) {
        this.gender = gender;
    }

    public void changeBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void changeFamilyRole(FamilyRole familyRole) {
        this.familyRole = familyRole;
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
            .birthDate(request.birthDate())
            .gender(request.gender())
            .profileImageUrl(request.profileImageUrl())
            .family(family)
            .role(Role.MEMBER)
            .isAlarmEnabled(true)
            .build();
    }
}
