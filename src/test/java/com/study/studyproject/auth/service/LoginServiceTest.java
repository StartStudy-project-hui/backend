package com.study.studyproject.auth.service;

import com.study.studyproject.global.exception.ex.BadRequestException;
import com.study.studyproject.global.exception.ex.DuplicateException;
import com.study.studyproject.global.hash.HashUtil;
import com.study.studyproject.email.repository.EmailVerificationRepository;
import com.study.studyproject.member.domain.Email;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.global.exception.ex.NotFoundException;
import com.study.studyproject.auth.dto.LoginRequest;
import com.study.studyproject.auth.dto.LoginResponseDto;
import com.study.studyproject.auth.dto.SignRequest;
import com.study.studyproject.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;

import static com.study.studyproject.auth.domain.Role.ROLE_USER;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class LoginServiceTest {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    LoginService loginService;

    @Autowired
    HttpServletResponse response;


    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();

    }

    private void markEmailVerified(String email) {
        String key = EmailVerificationRepository.VERIFIED_KEY_PREFIX + HashUtil.sha256(email);
        redisTemplate.opsForValue().set(key, "true", Duration.ofMinutes(30));
    }

    @Test
    @DisplayName("사용자 이메일과 비밀번호가 일치했을 때, 로그인을 성공한다.")
    void loginServiceTest() {

        //given
        String pwd = "!12341234";
        Member member1 = createMember("jacom2@naver.com", passwordEncoder.encode(pwd), "사용자명1", "닉네임1");
        memberRepository.save(member1);
        LoginRequest loginRequest = new LoginRequest(member1.getEmail().address(), pwd);

        //when
        LoginResponseDto loginResponseDto = loginService.loginService(loginRequest, response);

        //then
        Assertions.assertThat(loginResponseDto.getMessage()).isEqualTo("로그인 되었습니다.");
        Assertions.assertThat(loginResponseDto.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(loginResponseDto.getNickname()).isEqualTo(member1.getNickname());

    }


    @Test
    @DisplayName("비밀번호가 틀렸을 때, 로그인을 실패한다.")
    void wrongPwdLoginServiceTest() {

        //given
        Member member1 = createMember("jacom2@naver.com", passwordEncoder.encode("!12341234"), "사용자명1", "닉네임1");
        memberRepository.save(member1);

        LoginRequest loginRequest = new LoginRequest(member1.getEmail().address(), "wrongPwd");


        //when & then
        assertThatThrownBy(() -> loginService.loginService(loginRequest, response))
                .isInstanceOf(NotFoundException.class);
    }
    
    @Test
    @DisplayName("비밀번호와 비밀번호 확인 일치하고 중복된 회원가입이 아니며 이메일 인증을 마쳤을 경우, 회원 가입 성공한다.")
    void signTest() throws Exception {
            // given
            SignRequest signRequest = new SignRequest(
                    "김하임",
                    "jacom2@naver.com",
                    "닉네임1",
                    "!12341234",
                    "!12341234"
            );
            markEmailVerified(signRequest.getEmail());

            // when
            loginService.sign(signRequest);

            // then
            Member member = memberRepository.findByEmail(new Email(signRequest.getEmail()))
                    .orElseThrow();

            assertThat(member)
                    .extracting(
                            Member::getUsername,
                            m -> m.getEmail().address(),
                            Member::getNickname
                    )
                    .containsExactly(
                            "김하임",
                            "jacom2@naver.com",
                            "닉네임1"
                    );

        }

    @Test
    @DisplayName("비밀번호와 비밀번호 확인이 다른 경우, 회원 가입 실패한다.")
    void differentPwdSignTest() throws Exception {
        //given
        SignRequest signRequest = new SignRequest("김하임", "jacom2@naver.com", "닉네임1", "!12341234", "341234");


        //when & then
        assertThatThrownBy(() -> loginService.sign(signRequest))
                .isInstanceOf(NotFoundException.class);

    }


    @Test
    @DisplayName("중복된 회원일 경우, 회원 가입 실패한다.")
    void duplicationSignTest() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", passwordEncoder.encode("!12341234"), "사용자명1", "닉네임1");
        SignRequest signRequest = new SignRequest("김하임", "jacom2@naver.com", "닉네임2", "!12341234", "!12341234");
        memberRepository.save(member1);


        //when & then
        assertThatThrownBy(() -> loginService.sign(signRequest))
                .isInstanceOf(DuplicateException.class);

    }

    @Test
    @DisplayName("이메일 인증을 하지 않은 경우, 회원 가입 실패한다.")
    void notVerifiedSignTest() throws Exception {
        //given
        SignRequest signRequest = new SignRequest("김하임", "jacom2@naver.com", "닉네임1", "!12341234", "!12341234");

        //when & then
        assertThatThrownBy(() -> loginService.sign(signRequest))
                .isInstanceOf(BadRequestException.class);

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