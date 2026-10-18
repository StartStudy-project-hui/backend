package com.study.studyproject.email.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailSenderTest {

    @InjectMocks
    private EmailSender emailSender;

    @Mock
    private JavaMailSender mailSender;

    private static final String EMAIL = "test@test.com";

    @Test
    @DisplayName("발송에 성공하면 JavaMailSender로 메일을 전달한다")
    void sendSuccess() {
        // when
        emailSender.send(EMAIL, "123456");

        // then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("발송에 실패해도 예외를 삼키고 호출자에게 전파하지 않는다")
    void sendFailureIsSwallowed() {
        // given
        MailSendException exception = new MailSendException("SMTP 연결 실패");
        willThrow(exception).given(mailSender).send(any(SimpleMailMessage.class));

        // when & then
        assertThatCode(() -> emailSender.send(EMAIL, "123456")).doesNotThrowAnyException();
    }
}
