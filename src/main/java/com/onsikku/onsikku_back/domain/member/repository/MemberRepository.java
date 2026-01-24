package com.onsikku.onsikku_back.domain.member.repository;

import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, String> {
    Optional<Member> findByKakaoId(String kakaoId);

    @Query("SELECT m FROM Member m JOIN FETCH m.family WHERE m.id = :memberId")
    Optional<Member> findMemberWithFamily(@Param("memberId") UUID memberId);

    Optional<Member> findById(UUID memberId);
    boolean existsByKakaoId(String kakaoId);
    void deleteById(UUID memberId);

    List<Member> findAllByFamily_Id(UUID familyId);

    List<UUID> findByFamily_Id(UUID familyId);
}
