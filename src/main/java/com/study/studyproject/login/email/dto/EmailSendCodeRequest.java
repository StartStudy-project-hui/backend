package com.study.studyproject.login.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailSendCodeRequest {

    @NotBlank(message = "이메일을 입력해주세요")
    @Email
    private String email;

    @Builder
    public EmailSendCodeRequest(String email) {
        this.email = email;
    }
}
