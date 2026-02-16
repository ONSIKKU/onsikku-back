package com.onsikku.onsikku_back.domain.member.service;

import com.onsikku.onsikku_back.domain.answer.domain.Report;
import com.onsikku.onsikku_back.domain.answer.repository.ReportRepository;
import com.onsikku.onsikku_back.domain.member.domain.Block;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.dto.BlockRequest;
import com.onsikku.onsikku_back.domain.member.dto.ReportRequest;
import com.onsikku.onsikku_back.domain.member.repository.BlockRepository;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.question.dto.BlockedMemberResponse;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SafetyService {

    private final ReportRepository reportRepository;
    private final BlockRepository blockRepository;
    private final MemberRepository memberRepository;

    // 신고하기
    @Transactional
    public void createReport(UUID reporterId, ReportRequest request) {
        Report report = Report.builder()
                .reporterId(reporterId)
                .targetId(request.getTargetId())
                .targetType(request.getTargetType())
                .reason(request.getReason())
                .build();
        
        reportRepository.save(report);
        // 실제 운영 시엔 여기서 Slack 알림이나 Admin 이메일 발송 로직 추가
    }

    // 차단하기
    @Transactional
    public void blockUser(UUID blockerId, BlockRequest request) {
        UUID targetUserId = request.getBlockedId();

        if (blockerId.equals(targetUserId)) {
            throw new BaseException(BaseResponseStatus.CANNOT_SELF_BLOCK);
        }

        // 이미 차단했는지 확인
        if (!blockRepository.existsByBlockerIdAndBlockedId(blockerId, targetUserId)) {
            Block block = Block.builder()
                    .blockerId(blockerId)
                    .blockedId(targetUserId)
                    .build();
            blockRepository.save(block);
        }
    }

    // 차단 목록 가져오기 (필터링을 위해 사용)
    @Transactional(readOnly = true)
    public List<UUID> getRelatedWithBlockIds(UUID currentUserId) {
        // 내가 차단한 ID들
        List<UUID> blockedByMe = blockRepository.findBlockedIdsByBlockerId(currentUserId);

        // 나를 차단한 ID들
        List<UUID> blockingMe = blockRepository.findBlockerIdsByBlockedId(currentUserId);

        // 합치기 (Set을 사용해 중복 제거)
        Set<UUID> allBlockedIds = new HashSet<>();
        allBlockedIds.addAll(blockedByMe);
        allBlockedIds.addAll(blockingMe);

        return new ArrayList<>(allBlockedIds);
    }

    // 차단 해제
    @Transactional
    public void unblockUser(UUID blockerId, BlockRequest request) {
        blockRepository.deleteByBlockerIdAndBlockedId(blockerId, request.getBlockedId());
    }

    // 차단 목록 조회 (이름까지 같이 조회)
    @Transactional(readOnly = true)
    public List<BlockedMemberResponse> getBlockedMemberList(UUID blockerId) {
        // 차단 엔티티 목록 조회
        List<Block> blocks = blockRepository.findAllByBlockerId(blockerId);
        if(blocks.isEmpty()) {
            return List.of();
        }
        // ID를 이용해 Member 정보 조회 후 DTO 변환 (JPA 조인이나 스트림 사용)
        // TODO : 성능 개선 고려
        return blocks.stream()
            .map(block -> {
                Member blockedMember = memberRepository.findById(block.getBlockedId())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

                return BlockedMemberResponse.builder()
                    .blockedId(blockedMember.getId())
                    .nickname(blockedMember.getNickname())
                    .build();
            })
            .collect(Collectors.toList());
    }
}