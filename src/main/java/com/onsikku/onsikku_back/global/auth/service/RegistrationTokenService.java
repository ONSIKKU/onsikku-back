package com.onsikku.onsikku_back.global.auth.service;

import com.onsikku.onsikku_back.global.auth.dto.SocialMemberInfo;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.redis.RedisService;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationTokenService {

  private final RedisService redisService;
  @Value("${redis.registration-token.minutes}")
  private long minutes;

  public String save(SocialMemberInfo info) {
    String token = UUID.randomUUID().toString();
    redisService.set(token, info, Duration.ofMinutes(minutes));
    return token;
  }

  public SocialMemberInfo get(String token) {
    SocialMemberInfo info = redisService.get(token, SocialMemberInfo.class);
    if (info == null) throw new BaseException(BaseResponseStatus.INVALID_REGISTRATION_TOKEN);
    return info;
  }

  public void delete(String token) {
    redisService.delete(token);
  }
}
