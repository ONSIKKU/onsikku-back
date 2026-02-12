package com.onsikku.onsikku_back.global.auth.dto;

import com.onsikku.onsikku_back.domain.member.domain.SocialType;
import lombok.Builder;

@Builder
public record SocialMemberInfo(String socialId, SocialType socialType) {}