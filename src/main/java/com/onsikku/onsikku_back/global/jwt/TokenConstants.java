package com.onsikku.onsikku_back.global.jwt;

/**
 * JWT 및 Refresh Token, 블랙리스트 관리에 사용되는 상수들을 정의합니다.
 */
public class TokenConstants {
    // ------------------- JWT Type (Claims) -------------------
    public static final String TOKEN_TYPE_CLAIM = "type";
    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";

    // ------------------- Redis Key Prefixes -------------------
    /** Refresh Token 저장소 키 접두사 (RT:memberId) */
    public static final String RT_KEY_PREFIX = "RT:";

    /** Access Token 블랙리스트 키 접두사 (AT_BL:accessTokenString) */
    public static final String AT_BLACKLIST_PREFIX = "AT_BL:";

    // ----------------------------------------------------------
}