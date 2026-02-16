package com.onsikku.onsikku_back.domain.member.dto;

import com.onsikku.onsikku_back.domain.answer.domain.ReportReason;
import com.onsikku.onsikku_back.domain.answer.domain.ReportTargetType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReportRequest {
    private UUID targetId;    // 신고할 대상의 ID
    private ReportTargetType targetType;    // "ANSWER", "COMMENT", "USER"
    private ReportReason reason;            // "욕설/비방", "스팸", "부적절한 콘텐츠"
}