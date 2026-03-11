package com.onsikku.onsikku_back.domain.member.repository;


import com.onsikku.onsikku_back.domain.member.domain.Family;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FamilyRepository extends JpaRepository<Family, UUID>{
    Optional<Family> findByInvitationCode(String invitationCode);
    Optional<Family> findByInvitationCodeAndWithdrawnAtIsNull(String invitationCode);
    boolean existsByInvitationCode(String invitationCode);
    Page<Family> findAllByWithdrawnAtIsNull(Pageable pageable);
}
