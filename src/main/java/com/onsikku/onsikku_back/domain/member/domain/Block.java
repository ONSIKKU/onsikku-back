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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"blockerId", "blockedId"}) // 중복 차단 방지
})
public class Block extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID blockerId; // 차단한 사람 (나)

    @Column(nullable = false)
    private UUID blockedId; // 차단당한 사람 (보기 싫은 가족)
}