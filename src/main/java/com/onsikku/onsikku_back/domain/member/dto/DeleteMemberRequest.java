package com.onsikku.onsikku_back.domain.member.dto;

import com.onsikku.onsikku_back.domain.member.domain.WithdrawalReason;
import java.util.List;

public record DeleteMemberRequest(
    List<WithdrawalReason> reasons,
    String reasonDetail
) {
}
