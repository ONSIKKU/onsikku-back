package com.onsikku.onsikku_back.domain.notification.service;

import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.notification.dto.FcmTokenRequest;
import com.onsikku.onsikku_back.domain.notification.entity.FcmToken;
import com.onsikku.onsikku_back.domain.notification.repository.FcmTokenRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class FcmTokenService {
  private final FcmTokenRepository fcmTokenRepository;
  private final MemberRepository memberRepository;

  // FCM 토큰 삭제
  @Transactional
  public void deleteFcmTokenAndUpdateMember(FcmTokenRequest request) {
    // 요청 DeviceType에 저장된 토큰 조회
    FcmToken fcmToken = fcmTokenRepository.findByMemberAndDeviceType(request.getMember(), request.getDeviceType())
        .orElseThrow(()-> new BaseException(BaseResponseStatus.FCM_TOKEN_NOT_FOUND));
    fcmTokenRepository.delete(fcmToken);

    Member member = request.getMember();
    member.changeAlarmEnabled(false);
    memberRepository.save(member);
    log.debug("FCM 토큰 삭제: 회원: {}, 기기: {}", member.getId(), request.getDeviceType());
  }

  // FCM 토큰 저장
  @Transactional
  public void saveFcmTokenAndUpdateMember(FcmTokenRequest request) {
    // 요청 DeviceType에 저장된 토큰 조회
    FcmToken fcmToken = fcmTokenRepository.findByMemberAndDeviceType(request.getMember(), request.getDeviceType())
      .orElseGet(() -> FcmToken.createFromRequest(request));
    fcmToken.updateToken(request.getFcmToken());
    fcmTokenRepository.save(fcmToken);

    Member member = request.getMember();
    member.changeAlarmEnabled(true);
    memberRepository.save(member);
    log.debug("FCM 토큰 저장: 회원: {}, 기기: {}", member.getId(), request.getDeviceType());
  }
}
