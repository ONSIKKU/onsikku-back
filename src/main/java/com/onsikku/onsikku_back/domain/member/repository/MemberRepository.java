package com.onsikku.onsikku_back.domain.member.repository;

import com.onsikku.onsikku_back.domain.member.domain.Family;
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
    void deleteById(UUID memberId);

    List<Member> findAllByFamily_Id(UUID familyId);

    @Query("SELECT m.id FROM Member m WHERE m.family.id = :familyId")
    List<UUID> findByFamily_Id(UUID familyId);

    Optional<Member> findBySocialIdAndSocialType(String socialId, SocialType socialType);
}
