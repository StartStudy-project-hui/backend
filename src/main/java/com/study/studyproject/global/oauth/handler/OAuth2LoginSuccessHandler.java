package com.study.studyproject.global.oauth.handler;

import com.study.studyproject.global.security.UserDetailsImpl;
import com.study.studyproject.global.jwt.JwtUtil;
import com.study.studyproject.auth.dto.TokenDtoResponse;
import com.study.studyproject.auth.service.RefreshTokenService;
import com.study.studyproject.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;

    @Value("${location}")
    private String location;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserDetailsImpl oAuth2User = (UserDetailsImpl) authentication.getPrincipal();

        if (memberRepository.findById(oAuth2User.getMemberId()).isEmpty()) {
            log.warn("OAuth2 로그인 성공했지만 회원 정보를 찾을 수 없음: memberId={}", oAuth2User.getMemberId());
            redirect(response, false);
            return;
        }

        log.info("OAuth2 Login 성공!");
        TokenDtoResponse allToken = jwtUtil.createAllToken(oAuth2User.getEmail(), oAuth2User.getMemberId());
        refreshTokenService.saveOrUpdate(oAuth2User.getEmail().address(), allToken);
        jwtUtil.setCookie(response, allToken);
        redirect(response, true);
    }

    private void redirect(HttpServletResponse response, boolean loginSuccess) throws IOException {
        OAuth2LoginRedirector.redirect(response, location, loginSuccess);
    }

}
