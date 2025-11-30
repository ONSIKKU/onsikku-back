package com.onsikku.onsikku_back.global.auth.service;

import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String memberId) throws BaseException {
        Member member = memberRepository.findById(UUID.fromString(memberId))
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        return new CustomUserDetails(member);
    }
}
