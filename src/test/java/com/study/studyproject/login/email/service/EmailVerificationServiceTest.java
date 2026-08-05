package com.study.studyproject.login.email.service;

import com.study.studyproject.global.exception.ex.BadRequestException;
import com.study.studyproject.global.hash.HashUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private JavaMailSender mailSender;

    private static final String EMAIL = "test@test.com";

    @Test
    @DisplayName("쿨다운 상태가 아니면 인증번호를 발송하고 코드/쿨다운 키를 저장한다")
    void sendCodeSuccess() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(EmailVerificationService.COOLDOWN_KEY_PREFIX + HashUtil.sha256(EMAIL)))
                .willReturn(null);

        // when
        emailVerificationService.sendCode(EMAIL);

        // then
        verify(valueOperations).set(
                eq(EmailVerificationService.CODE_KEY_PREFIX + HashUtil.sha256(EMAIL)),
                any(), eq(Duration.ofMinutes(5)));
        verify(valueOperations).set(
                eq(EmailVerificationService.COOLDOWN_KEY_PREFIX + HashUtil.sha256(EMAIL)),
                eq("true"), eq(Duration.ofSeconds(60)));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()).contains(EMAIL);
    }

    @Test
    @DisplayName("쿨다운 상태이면 인증번호 재전송을 거부한다")
    void sendCodeCooldown() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(EmailVerificationService.COOLDOWN_KEY_PREFIX + HashUtil.sha256(EMAIL)))
                .willReturn("true");

        // when & then
        assertThatThrownBy(() -> emailVerificationService.sendCode(EMAIL))
                .isInstanceOf(BadRequestException.class);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("저장된 코드와 일치하면 인증에 성공하고 인증완료 키를 저장한다")
    void verifyCodeSuccess() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(EmailVerificationService.CODE_KEY_PREFIX + HashUtil.sha256(EMAIL)))
                .willReturn("123456");

        // when
        emailVerificationService.verifyCode(EMAIL, "123456");

        // then
        verify(redisTemplate).delete(EmailVerificationService.CODE_KEY_PREFIX + HashUtil.sha256(EMAIL));
        verify(valueOperations).set(
                eq(EmailVerificationService.VERIFIED_KEY_PREFIX + HashUtil.sha256(EMAIL)),
                eq("true"), eq(Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("저장된 코드와 일치하지 않으면 인증에 실패한다")
    void verifyCodeMismatch() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(EmailVerificationService.CODE_KEY_PREFIX + HashUtil.sha256(EMAIL)))
                .willReturn("123456");

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyCode(EMAIL, "000000"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("요청한 적 없거나 만료된 코드는 인증에 실패한다")
    void verifyCodeNotFound() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(EmailVerificationService.CODE_KEY_PREFIX + HashUtil.sha256(EMAIL)))
                .willReturn(null);

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyCode(EMAIL, "123456"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("인증완료 키가 있으면 isVerified는 true를 반환한다")
    void isVerifiedTrue() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(EmailVerificationService.VERIFIED_KEY_PREFIX + HashUtil.sha256(EMAIL)))
                .willReturn("true");

        // when & then
        assertThat(emailVerificationService.isVerified(EMAIL)).isTrue();
    }

    @Test
    @DisplayName("인증완료 키가 없으면 isVerified는 false를 반환한다")
    void isVerifiedFalse() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(EmailVerificationService.VERIFIED_KEY_PREFIX + HashUtil.sha256(EMAIL)))
                .willReturn(null);

        // when & then
        assertThat(emailVerificationService.isVerified(EMAIL)).isFalse();
    }

    @Test
    @DisplayName("clearVerification은 인증완료 키를 삭제한다")
    void clearVerification() {
        // when
        emailVerificationService.clearVerification(EMAIL);

        // then
        verify(redisTemplate).delete(EmailVerificationService.VERIFIED_KEY_PREFIX + HashUtil.sha256(EMAIL));
    }
}
