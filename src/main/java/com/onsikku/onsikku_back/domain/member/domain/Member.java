package com.onsikku.onsikku_back.domain.member.domain;


import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Family family;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Gender gender;

    private LocalDate birthDate;

    private FamilyRole familyRole;    // 가족 내 관계 (예: 엄마, 첫째 아들 등)

    private String profileImageUrl;

    private String kakaoId;
}