package com.onsikku.onsikku_back.domain.member.repository;


import com.onsikku.onsikku_back.domain.member.domain.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FamilyRepository extends JpaRepository<Family, UUID>{
    Optional<Family> findByInvitationCode(String invitationCode);
    boolean existsByInvitationCode(String invitationCode);
}