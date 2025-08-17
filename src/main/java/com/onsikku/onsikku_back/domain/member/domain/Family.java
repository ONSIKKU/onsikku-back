package com.onsikku.onsikku_back.domain.member.domain;


import com.onsikku.onsikku_back.global.entity.BaseEntity;
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
}