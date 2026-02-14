package com.onsikku.onsikku_back.domain.answer.repository;

import com.onsikku.onsikku_back.domain.answer.domain.Reaction;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {
    // 특정 답변에 특정 사용자가 남긴 반응 조회
    boolean existsByAnswer_IdAndMember_Id(UUID answerId, UUID memberId);

    // 벌크 삭제용 (가족 삭제 시 필요)
    @Modifying
    @Query("DELETE FROM Reaction r WHERE r.answer.id IN " +
           "(SELECT a.id FROM Answer a WHERE a.memberQuestion.family.id = :familyId)")
    void deleteByFamilyId(@Param("familyId") UUID familyId);

    @Query("SELECT r FROM Reaction r LEFT JOIN FETCH r.member WHERE r.answer.id = :answerId")
    List<Reaction> findAllByAnswer_Id(UUID answerId);

    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.answer.memberQuestion.family.id = :familyId " +
        "AND r.answer.memberQuestion.createdAt BETWEEN :start AND :end")
    int countMonthlyReactions(@Param("familyId") UUID familyId,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    Optional<Reaction> findByAnswer_Id(UUID answerId);

    int deleteAllByMember(Member member);
}