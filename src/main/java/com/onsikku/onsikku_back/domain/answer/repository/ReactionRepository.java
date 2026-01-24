package com.onsikku.onsikku_back.domain.answer.repository;

import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.domain.Reaction;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {
    // 특정 답변에 특정 사용자가 남긴 반응 조회
    boolean existsByAnswer_IdAndMember_Id(UUID answerId, UUID memberId);

    // 벌크 삭제용 (가족 삭제 시 필요)
    @Modifying
    @Query("DELETE FROM Reaction r WHERE r.answer.id IN " +
           "(SELECT a.id FROM Answer a WHERE a.memberQuestion.family.id = :familyId)")
    void deleteByFamilyId(@Param("familyId") Long familyId);
}