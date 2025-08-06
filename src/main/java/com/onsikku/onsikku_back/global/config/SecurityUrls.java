package com.onsikku.onsikku_back.global.config;

public class SecurityUrls {
  public static final String[] AUTH_WHITELIST = {
      "/api/auth/**",             // 회원가입, 로그인 관련
      "/api/login",               // redirect 로그인 페이지
      "/v3/api-docs/**",          // Swagger OpenAPI 문서 JSON
      "/swagger-ui/**",           // Swagger UI
      "/swagger-ui.html",         // Swagger UI HTML 진입점
      "/webjars/**",              // Swagger static 자원
      "/docs/**"                  // 문서 관련 접근 허용
  };

  /**
   * 관리자 권한이 필요한 URL 패턴 목록
   */
  public static final String[] ADMIN_PATHS = {

  };
}
