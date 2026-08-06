package com.study.studyproject.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenDtoResponse {

    private String accessToken;
    private String refreshToken;

    @Builder
    public TokenDtoResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static TokenDtoResponse of(String accessToken, String refreshToken){
        return TokenDtoResponse.builder().accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
