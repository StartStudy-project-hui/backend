package com.study.studyproject.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.studyproject.blacklist.domain.BlackList;
import com.study.studyproject.blacklist.repository.blacklist.BlackListRepository;
import com.study.studyproject.global.exception.ErrorResponseWriter;
import com.study.studyproject.global.hash.HashUtil;
import com.study.studyproject.global.exception.ex.ErrorCode;
import com.study.studyproject.member.domain.Email;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Duration BLACKLIST_CACHE_TTL = Duration.ofMinutes(5);
    private static final String RENEW_TOKEN_URI = "/api/renew-token";

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final BlackListRepository blackListRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 토큰 재발급 요청은 액세스 토큰이 만료된 상태로 들어오는 게 정상이므로,
        // 여기서 검증하지 않고 LoginService.renewalAccessToken()에 판단을 맡긴다.
        if (isRenewTokenRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = jwtUtil.resolveToken(jwtUtil.getHeaderToken(request, JwtUtil.ACCESS_TOKEN));

        // 1. 익명 사용자 확인
        if (isAnonymousUser(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }
        // 2. 토큰 유효성 검증
        if (isInvalidToken(accessToken)) {
            ErrorResponseWriter.write(response, objectMapper, ErrorCode.TOKEN_EXPIRED);
            return;
        }
        // 3. 블랙리스트 확인
        Email email = jwtUtil.getEmailFromToken(accessToken);
        if (isBlacklisted(email)) {
            ErrorResponseWriter.write(response, objectMapper, ErrorCode.BLACKLIST_USER);
            return;
        }

        // 4. 인증 정보 설정
        setAuthentication(email);
        filterChain.doFilter(request, response);
    }

    private boolean isInvalidToken(String accessToken) {
        return !jwtUtil.isAccessTokenValid(accessToken);
    }

    private static boolean isAnonymousUser(String accessToken) {
        return accessToken == null;
    }

    private static boolean isRenewTokenRequest(HttpServletRequest request) {
        return RENEW_TOKEN_URI.equals(request.getRequestURI());
    }

    private boolean isBlacklisted(Email email) {
        String hashValue = HashUtil.sha256(email.address());
        String cacheKey = BlackList.BLACKLIST_CACHE_KEY_PREFIX + hashValue;

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        Object cached = ops.get(cacheKey);
        if (cached instanceof String cachedBlocked) {
            return Boolean.parseBoolean(cachedBlocked);
        }

        boolean blocked = blackListRepository.findByHashValue(hashValue)
                .map(BlackList::isBlocked)
                .orElse(false);
        ops.set(cacheKey, String.valueOf(blocked), BLACKLIST_CACHE_TTL);
        return blocked;
    }

    private void setAuthentication(Email email) {
        Authentication authentication = jwtUtil.createAuthentication(email.address());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
