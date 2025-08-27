package com.onsikku.onsikku_back.global.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.global.auth.dto.KakaoMemberInfo;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuth2Service {

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${oauth2.kakao.client-id}")
  private String clientId;

  @Value("${oauth2.kakao.redirect-uri}")
  private String redirectUri;

  @Value("${oauth2.kakao.authorization-grant-type}")
  private String grantType;

  public KakaoMemberInfo getKakaoMemberInfoFromCode(String code) {
    String accessToken = getAccessToken(code);

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      HttpEntity<Void> entity = new HttpEntity<>(headers);

      ResponseEntity<JsonNode> response = restTemplate.exchange(
          "https://kapi.kakao.com/v2/user/me",
          HttpMethod.GET,
          entity,
          JsonNode.class
      );

      String kakaoId = response.getBody().get("id").asText();
      return new KakaoMemberInfo(kakaoId);

    } catch (HttpClientErrorException e) {
      log.warn("Failed to fetch user info from Kakao: status={}, body={}",
          e.getStatusCode(), e.getResponseBodyAsString());
      throw new BaseException(BaseResponseStatus.INVALID_ACCESS_TOKEN);
    } catch (Exception e) {
      log.error("Exception while fetching user info from Kakao", e);
      throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private String getAccessToken(String code) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.add("grant_type", grantType);
      params.add("client_id", clientId);
      params.add("redirect_uri", redirectUri);
      params.add("code", code);

      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
      ResponseEntity<JsonNode> response = restTemplate.exchange(
          "https://kauth.kakao.com/oauth/token",
          HttpMethod.POST,
          request,
          JsonNode.class
      );
      return response.getBody().get("access_token").asText();

    } catch (HttpClientErrorException e) {
      String body = e.getResponseBodyAsString();
      log.warn("Failed to request Kakao access token: {}", body);
      throw new BaseException(BaseResponseStatus.KAKAO_INVALID_GRANT);
    } catch (Exception e) {
      log.error("Unexpected Exception during Kakao token request", e);
      throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
    }
  }
}