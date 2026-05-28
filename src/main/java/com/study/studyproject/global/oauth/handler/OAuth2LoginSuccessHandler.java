package com.study.studyproject.global.oauth.handler;

import com.study.studyproject.global.auth.UserDetailsImpl;
import com.study.studyproject.global.jwt.JwtUtil;
import com.study.studyproject.login.domain.RefreshToken;
import com.study.studyproject.login.dto.LoginRequest;
import com.study.studyproject.login.dto.TokenDtoResponse;
import com.study.studyproject.login.repository.RefreshRepository;
import com.study.studyproject.member.domain.Email;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshRepository refreshRepository;

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Value("${location}")
    private String location;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");

        UserDetailsImpl oAuth2User = (UserDetailsImpl) authentication.getPrincipal();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(location);

        Optional<Member> findMember = memberRepository.findById(oAuth2User.getMemberId());
        if (findMember.isEmpty()) { // 존재하지 않는 다면
            String redirectionUri = uriBuilder
                    .queryParam("loginSuccess", false)
                    .build()
                    .toUriString();
            response.sendRedirect(redirectionUri);
        } else { // 존재한다면
            Member member = findMember.get();
            TokenDtoResponse allToken = jwtUtil.createAllToken(oAuth2User.getEmail(), oAuth2User.getMemberId());
            LoginRequest loginRequest = LoginRequest.from(oAuth2User.getEmail(), oAuth2User.getPassword());
            //리프레시 토큰에 넣기
            refreshRepository.findByAccessToken(allToken.getAccessToken())
                    .ifPresentOrElse(
                            token -> refreshRepository.save(token.updateToken(allToken.getRefreshToken())), // 존재한다면
                            () -> refreshRepository.save(RefreshToken.toEntity(allToken, loginRequest)) //존재하지 않으면
                    );
            jwtUtil.setCookie(response, allToken);
            String redirectionUri = uriBuilder
                    .queryParam("loginSuccess", true)
                    .build()
                    .toUriString();
            response.sendRedirect(redirectionUri);
        }

    }

}


