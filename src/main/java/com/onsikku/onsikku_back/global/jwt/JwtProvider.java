package com.onsikku.onsikku_back.global.jwt;

import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.onsikku.onsikku_back.global.jwt.TokenConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtProvider {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration}")
    private long jwtAccessExpirationInMs;

    @Getter
    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpirationInMs;

    private Key key;


    // secret key 초기화
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // JWT 생성: memberId를 subject로, relation 등을 claim으로 포함
    public String generateAccessTokenFromMember(Member member) {
        return generateTokenWithMs(member, jwtAccessExpirationInMs, ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshTokenFromMember(Member member) {
        return generateTokenWithMs(member, jwtRefreshExpirationInMs, REFRESH_TOKEN_TYPE);
    }

    public UUID getMemberIdFromClaims(Claims claims) {
        String memberIdStr = claims.getSubject();
        try {
            return UUID.fromString(memberIdStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format in JWT subject, cannot parse: {}", memberIdStr);
            // 이 토큰은 Subject 형식이 잘못되었으므로 유효하지 않은 토큰으로 처리합니다.
            throw new BaseException(BaseResponseStatus.INVALID_TOKEN);
        }
    }

    // 토큰 타입 검증
    public void validateTokenType(Claims claims, String expectedType) {
        String actualType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        if (actualType == null || !actualType.equals(expectedType)) {
            log.warn("Invalid token type detected. Expected: {}, Actual: {}", expectedType, actualType);
            throw new BaseException(BaseResponseStatus.INVALID_TOKEN_TYPE);
        }
    }

    // 토큰 유효성 검사
    public Claims validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BaseException(BaseResponseStatus.MISSING_AUTH_TOKEN);
        }
        try {
            return getClaims(token);
        } catch (SecurityException | MalformedJwtException ex) {
            log.warn("Invalid JWT signature: {}", ex.getMessage());
            throw new BaseException(BaseResponseStatus.INVALID_TOKEN);
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token: {}", ex.getMessage());
            throw new BaseException(BaseResponseStatus.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
            throw new BaseException(BaseResponseStatus.INVALID_TOKEN);
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: {}", ex.getMessage());
            throw new BaseException(BaseResponseStatus.MISSING_AUTH_TOKEN);
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

    private String generateTokenWithMs(Member member, long expirationInMs, String tokenType) {
        UUID memberId = member.getId();
        String role = member.getRole().name();
        String familyRole = member.getFamilyRole().toString();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInMs);

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
                "familyRole", familyRole,
                TOKEN_TYPE_CLAIM, tokenType
            ))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }
    /**
     * 토큰의 남은 만료 시간(밀리초)을 계산하여 반환합니다.
     */
    public long getRemainingExpirationInMs(Claims claims) {
        Date expiration = claims.getExpiration();
        Date now = new Date();
        long diff = expiration.getTime() - now.getTime();
        return Math.max(0, diff); // 0보다 작을 수 없으므로 max(0, diff) 처리
    }
    public String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer " (7자리) 이후의 문자열만 반환
            return bearerToken.substring(7);
        }
        return null;
    }
}