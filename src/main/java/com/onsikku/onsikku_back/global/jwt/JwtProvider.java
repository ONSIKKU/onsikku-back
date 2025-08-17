package com.onsikku.onsikku_back.global.jwt;

import com.onsikku.onsikku_back.domain.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.domain.auth.service.CustomUserDetailsService;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtProvider {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    private Key key;

    private final CustomUserDetailsService customUserDetailsService;

    // secret key 초기화
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // JWT 생성: memberId를 subject로, relation 등을 claim으로 포함
    public String generateTokenFromMember(Member member) {
        UUID memberId = member.getId();
        String role = member.getRole().name();
        String familyRole = member.getFamilyRole().toString();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
            .setHeaderParam("typ", "JWT")
            .setIssuer("onsikku-auth")                 // iss
            .setAudience("onsikku-api")                // aud
            .setId(UUID.randomUUID().toString())       // jti
            .setSubject(memberId.toString()) // UUID를 문자열로 변환하여 subject로 설정
            .setIssuedAt(now)
            .setNotBefore(now)                         // nbf
            .setExpiration(expiryDate)
            .addClaims(Map.of(
                "role", role,
                "familyRole", familyRole
            ))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public UsernamePasswordAuthenticationToken getAuthenticationToken(Claims claims) {
        String memberIdStr = claims.getSubject();
        log.info("Parsed memberId: {}", memberIdStr);
        CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(memberIdStr);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    // 토큰 유효성 검사
    public Claims validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BaseException(BaseResponseStatus.MISSING_ACCESS_TOKEN);
        }
        try {
            return getClaims(token);
        } catch (SecurityException | MalformedJwtException ex) {
            log.warn("Invalid JWT signature: {}", ex.getMessage());
            throw new BaseException(BaseResponseStatus.INVALID_ACCESS_TOKEN);
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token: {}", ex.getMessage());
            throw new BaseException(BaseResponseStatus.EXPIRED_ACCESS_TOKEN);
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
            throw new BaseException(BaseResponseStatus.INVALID_ACCESS_TOKEN);
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: {}", ex.getMessage());
            throw new BaseException(BaseResponseStatus.MISSING_ACCESS_TOKEN);
        }
    }

    // Claims 추출
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}