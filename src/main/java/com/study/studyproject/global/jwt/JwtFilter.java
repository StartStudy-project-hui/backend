package com.study.studyproject.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.studyproject.blacklist.repository.blacklist.BlackListRepository;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.hash.HashUtil;
import com.study.studyproject.global.exception.ex.ErrorCode;
import com.study.studyproject.member.domain.Email;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final BlackListRepository blackListRepository;


    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("로그인");

        String accessToken = jwtUtil.resolveToken(jwtUtil.getHeaderToken(request, JwtUtil.ACCESS_TOKEN));

        if (isAnonymouseUser(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }
        // 2. 토큰 유효성 검증
        if (isNotInvalidToken(accessToken)) {
            jwtExceptionHandler(response, ErrorCode.TOKEN_EXPIRED);
            return;
        }
        // 3. 블랙리스트 확인
        Email email = jwtUtil.getEmailFromToken(accessToken);
        if (isBlacklisted(email)) {
            jwtExceptionHandler(response, ErrorCode.BLACKLIST_USER);
            return;
        }

        // 4. 인증 정보 설정
        setAuthentication(email);
        filterChain.doFilter(request, response);


    }

    private boolean isNotInvalidToken(String accessToken) {
        return !jwtUtil.AccessTokenValidation(accessToken);
    }

    private static boolean isAnonymouseUser(String accessToken) {
        return accessToken == null;
    }


    /**
     * 에러 응답 공통 처리
     */
    private void jwtExceptionHandler(HttpServletResponse response, ErrorCode errorCode) {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try {
            String json = objectMapper.writeValueAsString(
                    new GlobalResultDto(errorCode.getMessage(), errorCode.getStatus().value())
            );
            response.getWriter().write(json);
        } catch (IOException e) {
            log.error("JWT Filter Error Response Write Exception: {}", e.getMessage());
        }
    }


    private boolean isBlacklisted(Email email) {
        String hashValue = HashUtil.sha256(email.address());
        return blackListRepository.findByHashValue(hashValue)
                .map(blackList -> blackList.isBlocked())
                .orElse(false);
    }

    private void setAuthentication(Email email) {
        Authentication authentication = jwtUtil.createAuthentication(email.address());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


}
