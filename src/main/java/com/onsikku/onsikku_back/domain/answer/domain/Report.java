package com.onsikku.onsikku_back.domain.answer.domain;

import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable = false)
    private UUID reporterId; // 신고한 사람 ID

    @Column(nullable = false)
    private UUID targetId;   // 신고 대상 ID (답변 ID, 댓글 ID, 혹은 유저 ID)

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportReason reason;   // 신고 사유 (욕설, 스팸 등)
}