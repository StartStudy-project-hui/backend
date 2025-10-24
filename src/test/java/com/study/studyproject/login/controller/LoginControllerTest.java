package com.study.studyproject.login.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.global.jwt.JwtUtil;
import com.study.studyproject.login.dto.LoginRequest;
import com.study.studyproject.login.dto.SignRequest;
import com.study.studyproject.login.dto.TokenDtoResponse;
import com.study.studyproject.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.study.studyproject.login.domain.Role.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private  JwtUtil jwtUtil;
    @Autowired
    PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("사용자가 회원가입을 한다.")
    void sign() throws Exception {

        //then

        SignRequest signRequest = SignRequest.builder()
                .name("김하임")
                .email("jacom2@naver.com")
                .pwd("!12341234")
                .checkPwd("!12341234")
                .build();


        //when then
        mockMvc.perform(post("/api/v1/auth/sign")
                        .content(objectMapper.writeValueAsString(signRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입 성공"));
    }


    @Test
    @DisplayName("사용자가 회원가입에 이름은 필수값입니다.")
    void signWithoutName() throws Exception {



        //given

        SignRequest signRequest = SignRequest.builder()
                .email("jacom2@naver.com")
                .pwd("!12341234")
                .checkPwd("!12341234")
                .build();
        //when & then
        mockMvc.perform(post("/api/v1/auth/sign")
                        .content(objectMapper.writeValueAsString(signRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("사용자가 비밀번호와 비밀번호 체크는 필수입니다.")
    void signWithoutPwdAndCheckPwd() throws Exception {


        //given

        SignRequest signRequest = SignRequest.builder()
                .name("김하")
                .email("jacom2@naver.com")
                .build();

        //when & then
        mockMvc.perform(post("/api/v1/auth/sign")
                        .content(objectMapper.writeValueAsString(signRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인을 한다.")
    void login() throws Exception {
        Member member1 = createMember("jacom2@naver.com", passwordEncoder.encode("!12341234"), "사용자명1", "닉네임1");
        memberRepository.save(member1);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(member1.getEmail())
                .pwd("!12341234")
                .build();

        //when then
        //when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("로그인을 할 때, 이메일과 비밀번호는 필수값이다.")
    void loginWithoutEmailAndPwd() throws Exception {
        Member member1 = createMember("jacom2@naver.com", passwordEncoder.encode("!12341234"), "사용자명1", "닉네임1");
        memberRepository.save(member1);
        LoginRequest loginRequest = LoginRequest.builder()
                .build();

        //when then
        //when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("로그아웃을 한다.")
    void logout() throws Exception {
        //given
        Member member = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        memberRepository.save(member);
        LoginRequest loginRequest = LoginRequest.builder()
                .build();

        TokenDtoResponse allToken = jwtUtil.createAllToken(member.getEmail(), member.getId());

        //when & then
        mockMvc.perform(post("/api/v1/auth/service-logout")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."))
                .andExpect(jsonPath("$.statusCode").value("200"));
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
