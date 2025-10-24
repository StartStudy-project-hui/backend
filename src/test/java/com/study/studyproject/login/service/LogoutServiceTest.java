package com.study.studyproject.login.service;

import com.study.studyproject.member.domain.Member;
import com.study.studyproject.login.domain.RefreshToken;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.jwt.JwtUtil;
import com.study.studyproject.login.dto.TokenDtoResponse;
import com.study.studyproject.login.repository.RefreshRepository;
import com.study.studyproject.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.study.studyproject.login.domain.Role.ROLE_USER;

@Transactional
@SpringBootTest
class LogoutServiceTest {

    @Autowired
    LogoutService logoutService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RefreshRepository refreshRepository;


    @Autowired
    PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        refreshRepository.deleteAll();

    }

    @Test
    @DisplayName("로그아웃 버튼을 누를 시, 로그아웃이 된다.")
    void logoutService() {
        //given
        Member member1 = createMember("jacom2@naver.com", passwordEncoder.encode("!12341234"), "사용자명1", "닉네임1");
        TokenDtoResponse allToken = jwtUtil.createAllToken(member1.getEmail(), member1.getId());
        memberRepository.save(member1);
        RefreshToken getRefreshToken = getRefreshToken(allToken, member1);
        refreshRepository.save(getRefreshToken);


        //when
        GlobalResultDto globalResultDto = logoutService.logoutService(member1.getEmail());

        //then

        Assertions.assertThat(globalResultDto.getMessage()).isEqualTo("로그아웃 되었습니다.");
        Assertions.assertThat(globalResultDto.getStatusCode()).isEqualTo(HttpStatus.OK.value());
    }



    private  RefreshToken getRefreshToken(TokenDtoResponse allToken, Member member1) {
        return RefreshToken.builder()
                .refreshToken(allToken.getRefreshToken())
                .accessToken(allToken.getAccessToken())
                .email(member1.getEmail())
                .build();
    }

    private Member createMember
            (String email, String password, String username, String nickname) {
        {
            return Member.builder()
                    .nickname(nickname)
                    .username(username)
                    .email(email)
                    .password(password)
                    .role(ROLE_USER).build();
        }

    }
}