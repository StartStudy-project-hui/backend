package com.study.studyproject.login.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignRequest {

    @NotBlank(message = "이름을 입력해주세요")
    private String name;

    @NotBlank(message = "이메일을 입력해주세요")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String pwd;

    @NotBlank(message = "비밀번호 확인을 입력해주세요")
    private String checkPwd;

    @Builder
    public SignRequest(String name, String email, String pwd, String checkPwd) {
        this.name = name;
        this.email = email;
        this.pwd = pwd;
        this.checkPwd = checkPwd;
    }
}
