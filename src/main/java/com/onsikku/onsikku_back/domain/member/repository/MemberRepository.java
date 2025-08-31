package com.onsikku.onsikku_back.domain.member.repository;

import com.onsikku.onsikku_back.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, String> {
    Optional<Member> findByKakaoId(String kakaoId);
    @Query("SELECT m FROM Member m JOIN FETCH m.family WHERE m.id = :memberId")
    Optional<Member> findMemberWithFamily(@Param("memberId") UUID memberId);
    Optional<Member> findById(UUID memberId);
    boolean existsByKakaoId(String kakaoId);
    void deleteById(UUID memberId);
}
