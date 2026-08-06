package com.study.studyproject.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponseDto {

    @Schema(description = "메시지", defaultValue = "~~ 완료")
    private String message;
    @Schema(description = "상태코드", defaultValue = "200")
    private int statusCode;

    @Schema(description = "닉네임", defaultValue = "jac")
    private String nickname;

    public LoginResponseDto(String message, int statusCode, String nickname) {
        this.message = message;
        this.statusCode = statusCode;
        this.nickname = nickname;
    }


}
