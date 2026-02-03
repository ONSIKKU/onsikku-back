package com.onsikku.onsikku_back.domain.question.domain;

import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@DynamicUpdate  // 변경된 필드만 업데이트
public class QuestionCycle extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<UUID> memberOrderList;     // 랜덤하게 섞인 멤버 ID 리스트 [ID_B, ID_A, ID_D, ID_C]

    private int currentIndex;               // 현재 몇 번째 멤버가 주인공인지 (0, 1, 2...)

    private boolean active;                 // 현재 이 사이클이 유효한지

    public UUID getTodayMemberId() {
        return memberOrderList.get(currentIndex);
    }

    public void incrementIndex() {
        this.currentIndex++;
        if (this.currentIndex >= memberOrderList.size()) {
            this.active = false; // 사이클 종료
        }
    }

    public static QuestionCycle createNewCycle(Family family, List<UUID> members) {
        QuestionCycle cycle = QuestionCycle.builder()
            .family(family)
            .build();
        cycle.refreshCycle(members);
        return cycle;
    }

    public void refreshCycle(List<UUID> members) {
        if (members == null || members.isEmpty()) {
            this.memberOrderList = Collections.emptyList();
            this.active = false;
            return;
        }

        Collections.shuffle(members);       // 랜덤 셔플

        this.memberOrderList = members;     // 새 순서 저장
        this.currentIndex = 0;              // 인덱스 초기화
        this.active = true;                 // 사이클 활성화
    }

    public boolean isFinished() {
        return this.memberOrderList == null || this.currentIndex >= this.memberOrderList.size();
    }
}