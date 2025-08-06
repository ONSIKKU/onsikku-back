package com.onsikku.onsikku_back.domain.auth.dto;

public record KakaoSignupRequest(
    String registrationToken,
    String relation,
    String phoneNumber,
    String profileImageUrl,
    String familyName,
    String familyInvitationCode,
    String familyMode // CREATE or JOIN
) {}