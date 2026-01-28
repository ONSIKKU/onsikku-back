package com.onsikku.onsikku_back.global.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.global.auth.service.CustomUserDetailsService;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.redis.RedisService;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import com.onsikku.onsikku_back.global.response.ErrorResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static com.onsikku.onsikku_back.global.config.SecurityUrls.AUTH_WHITELIST;
import static com.onsikku.onsikku_back.global.jwt.TokenConstants.ACCESS_TOKEN_TYPE;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private static final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = jwtProvider.extractToken(request);
            log.info("Filtering request URI: {}", request.getRequestURI());
            Claims claims = jwtProvider.validateToken(token);
            jwtProvider.validateTokenType(claims, ACCESS_TOKEN_TYPE);

            // 블랙리스트 확인
            if (redisService.get(TokenConstants.AT_BLACKLIST_PREFIX + token, String.class) != null) {
                log.warn("Access Token is blacklisted: {}", token);
                throw new BaseException(BaseResponseStatus.TOKEN_BLACKLISTED);
            }
            // 사용자 정보 로드
            String memberIdStr = claims.getSubject();
            log.info("Parsed memberId: {}", memberIdStr);
            CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(memberIdStr);
            // 인증 토큰 생성
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // SecurityContext 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Member authenticated: role: {}, familyRole: {}", claims.get("role"), claims.get("familyRole"));
        } catch (BaseException ex) {
            log.warn("BaseException occurred during JWT auth: {}", ex.getStatus().getMessage());
            SecurityContextHolder.clearContext(); // 인증 실패 시 SecurityContext 초기화
            sendErrorResponse(response, ex.getStatus());
            return;
        } catch (Exception ex) {
            log.error("Unexpected exception during JWT auth", ex);
            SecurityContextHolder.clearContext(); // 인증 실패 시 SecurityContext 초기화
            sendErrorResponse(response, BaseResponseStatus.FAIL_TOKEN_AUTHORIZATION); // 또는 INTERNAL_SERVER_ERROR 등으로 처리
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 요청 URL이 AUTH_WHITELIST에 포함되어 있는지 확인
     * @param request HttpServletRequest 객체
     * @return true: 화이트리스트에 포함된 경우, false: 포함되지 않은 경우
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return Arrays.stream(AUTH_WHITELIST)
            .anyMatch(pattern -> matcher.match(pattern, path));
    }


    /**
     * 에러 응답을 JSON 형태로 클라이언트에 전송
     *
     * @param response  HttpServletResponse 객체
     * @param baseResponseStatus 발생한 에러코드
     * @throws IOException
     */
    private void sendErrorResponse(HttpServletResponse response, BaseResponseStatus baseResponseStatus) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(baseResponseStatus.getCode());
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = new ErrorResponse(baseResponseStatus, baseResponseStatus.getMessage());

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), errorResponse);
    }
}
