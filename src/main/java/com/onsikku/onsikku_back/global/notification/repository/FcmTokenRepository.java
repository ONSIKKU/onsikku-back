package com.onsikku.onsikku_back.global.notification.repository;

import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.global.notification.domain.FcmToken;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FcmTokenRepository extends JpaRepository<FcmToken, UUID> {
    // 특정 멤버 ID에 연결된 모든 FCM 토큰 리스트 조회
    List<FcmToken> findAllByMemberIdIn(List<UUID> memberIds);
}