package com.study.studyproject.login.dto;

import com.study.studyproject.member.domain.Email;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.dto.UserInfoResponseDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginRequest {

    @NotBlank(message = "아이디를 입력해주세요")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String pwd;

    @Builder
    public LoginRequest(String email, String pwd) {
        this.email = email;
        this.pwd = pwd;
    }

    public static LoginRequest from(Email email, String pwd) {
        return LoginRequest.builder()
                .email(email.address())
                .pwd(pwd)
                .build();
    }

}
