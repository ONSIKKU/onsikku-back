package com.onsikku.onsikku_back.domain.notification.repository;


import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.notification.entity.DeviceType;
import com.onsikku.onsikku_back.domain.notification.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FcmTokenRepository extends JpaRepository<FcmToken, UUID> {

  Optional<FcmToken> findByMemberAndDeviceType(Member member, DeviceType deviceType);

  List<FcmToken> findAllByMember(Member member);

  int deleteAllByMember_Id(UUID memberId);

  void deleteByToken(String token);
}
