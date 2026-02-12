package com.onsikku.onsikku_back.global.auth.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.onsikku.onsikku_back.domain.member.domain.SocialType;
import com.onsikku.onsikku_back.global.auth.dto.SocialMemberInfo;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppleOAuth2Service {
    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    @Value("${apple.bundle-id}")
    private String appleBundleId;

    /**
     * 프론트에서 받은 identityToken(String)을 검증하고 유저 정보를 추출
     * Apple은 AccessToken 보다 Identity Token이 인증 핵심
     */
    public SocialMemberInfo getAppleMemberInfo(String identityToken) {
        try {
            // Apple Public Keys 조회
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            JWKSet jwkSet = JWKSet.load(new URL(APPLE_KEYS_URL));
            
            // JWT 검증 설정 (서명 검증)
            JWSVerificationKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, new ImmutableJWKSet<>(jwkSet));
            jwtProcessor.setJWSKeySelector(keySelector);

            // 토큰 파싱 및 검증 수행
            JWTClaimsSet claims = jwtProcessor.process(identityToken, null);

            // 추가 클레임 검증
            // iss (발급자) 확인
            if (!APPLE_ISSUER.equals(claims.getIssuer())) {
                log.error("Invalid Issuer: {}", claims.getIssuer());
                throw new BaseException(BaseResponseStatus.APPLE_SOCIAL_LOGIN_FAILED);
            }

            // aud (대상) 확인 -> 내 앱을 위해 발급된 토큰인지 확인
            List<String> audience = claims.getAudience();
            if (audience == null || !audience.contains(appleBundleId)) {
                log.error("[CRITICAL][SECURITY] Apple Login Aud Mismatch! Expected: {}, Actual: {}", appleBundleId, audience);
                throw new BaseException(BaseResponseStatus.APPLE_SOCIAL_LOGIN_FAILED);
            }

            // 정보 추출 및 반환 (sub가 고유 ID)
            return new SocialMemberInfo(claims.getSubject(), SocialType.APPLE);
        } catch (MalformedURLException | ParseException | JOSEException | BadJOSEException e) {
            log.error("Apple Identity Token verification failed: {}", e.getMessage());
            throw new BaseException(BaseResponseStatus.APPLE_SOCIAL_LOGIN_FAILED); // 적절한 에러 코드로 변경 필요
        } catch (Exception e) {
             throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }
}