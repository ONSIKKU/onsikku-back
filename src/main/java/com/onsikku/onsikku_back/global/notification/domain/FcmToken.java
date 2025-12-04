package com.onsikku.onsikku_back.global.notification.domain;

import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "fcm_token",
    uniqueConstraints = @UniqueConstraint(name = "uq_member_device_token", columnNames = {"member_id", "token"})) // 토큰-멤버 ID 쌍의 유일성 보장
public class FcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "token", columnDefinition = "text", nullable = false)
    private String token; // 실제 FCM 기기 토큰

    @Column(name = "device_type")
    private String deviceType; // (선택적) iOS/Android 등 기기 종류
}