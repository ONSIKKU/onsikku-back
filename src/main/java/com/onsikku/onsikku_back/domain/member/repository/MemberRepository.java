package com.onsikku.onsikku_back.domain.member.repository;

import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.domain.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, String> {

    @Query("SELECT m FROM Member m JOIN FETCH m.family WHERE m.id = :memberId")
    Optional<Member> findMemberWithFamily(@Param("memberId") UUID memberId);

    Optional<Member> findById(UUID memberId);
    boolean existsBySocialId(String socialId);

    List<Member> findAllByFamily_Id(UUID familyId);
    List<Member> findAllByFamily_IdAndWithdrawnAtIsNull(UUID familyId);

    @Query("SELECT m.id FROM Member m WHERE m.family.id = :familyId AND m.withdrawnAt IS NULL")
    List<UUID> findActiveMemberIdsByFamilyId(@Param("familyId") UUID familyId);

   Optional<Member> findBySocialIdAndSocialTypeAndWithdrawnAtIsNull(String socialId, SocialType socialType);
    long countByFamily_IdAndWithdrawnAtIsNull(UUID familyId);
}
