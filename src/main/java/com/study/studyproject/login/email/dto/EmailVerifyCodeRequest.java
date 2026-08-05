package com.study.studyproject.login.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerifyCodeRequest {

    @NotBlank(message = "이메일을 입력해주세요")
    @Email
    private String email;

    @NotBlank(message = "인증번호를 입력해주세요")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증번호는 6자리 숫자입니다")
    private String code;

    @Builder
    public EmailVerifyCodeRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }
}
