package com.onsikku.onsikku_back.global.auth.service;

import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
        Member member = memberRepository.findById(UUID.fromString(memberId))
                .orElseThrow(() -> new UsernameNotFoundException("Member not found with memberId: " + memberId));

        return new CustomUserDetails(member);
    }
}
