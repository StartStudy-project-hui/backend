package com.study.studyproject.login.email.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.studyproject.global.hash.HashUtil;
import com.study.studyproject.login.email.dto.EmailVerifyCodeRequest;
import com.study.studyproject.login.email.service.EmailVerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 인증번호 발송(mailSender.send 호출) 관련 동작은 실제 SMTP 연동 없이
// EmailVerificationServiceTest(Mockito)에서 검증하고, 여기서는 verify-code 엔드포인트의
// 컨트롤러-서비스-Redis 연동만 확인한다.
@AutoConfigureMockMvc
@SpringBootTest
class EmailVerificationControllerTest {

    private static final String EMAIL = "verify-controller-test@naver.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    @DisplayName("인증번호가 일치하면 인증에 성공한다")
    void verifyCode() throws Exception {
        // given
        String key = EmailVerificationService.CODE_KEY_PREFIX + HashUtil.sha256(EMAIL);
        redisTemplate.opsForValue().set(key, "654321", Duration.ofMinutes(5));

        EmailVerifyCodeRequest request = EmailVerifyCodeRequest.builder()
                .email(EMAIL)
                .code("654321")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/auth/email/verify-code")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이메일 인증이 완료되었습니다."));
    }

    @Test
    @DisplayName("인증번호가 일치하지 않으면 400을 반환한다")
    void verifyCodeMismatch() throws Exception {
        // given
        String key = EmailVerificationService.CODE_KEY_PREFIX + HashUtil.sha256(EMAIL);
        redisTemplate.opsForValue().set(key, "654321", Duration.ofMinutes(5));

        EmailVerifyCodeRequest request = EmailVerifyCodeRequest.builder()
                .email(EMAIL)
                .code("000000")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/auth/email/verify-code")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("인증번호 형식이 6자리 숫자가 아니면 400을 반환한다")
    void verifyCodeInvalidFormat() throws Exception {
        // given
        EmailVerifyCodeRequest request = EmailVerifyCodeRequest.builder()
                .email(EMAIL)
                .code("12a")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/auth/email/verify-code")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
