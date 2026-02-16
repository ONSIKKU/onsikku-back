package com.onsikku.onsikku_back.domain.member.repository;

import com.onsikku.onsikku_back.domain.member.domain.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BlockRepository extends JpaRepository<Block, UUID> {
    // 내가 차단한 내역이 있는지 확인
    boolean existsByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);
    
    // 내가 차단한 사람들의 ID 리스트 조회 (필터링용)
    @Query("SELECT b.blockedId FROM Block b WHERE b.blockerId = :blockerId")
    List<UUID> findBlockedIdsByBlockerId(@Param("blockerId") UUID blockerId);
    @Query("SELECT b.blockerId FROM Block b WHERE b.blockedId = :blockedId")
    List<UUID> findBlockerIdsByBlockedId(@Param("blockedId") UUID blockedId);

    @Query("DELETE FROM Block b WHERE b.blockerId = :memberId OR b.blockedId = :memberId")
    void deleteByMemberId(UUID memberId);

    void deleteByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    List<Block> findAllByBlockerId(UUID blockerId);
}