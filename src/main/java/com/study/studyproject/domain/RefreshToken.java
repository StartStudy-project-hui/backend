package com.study.studyproject.domain;

import com.study.studyproject.login.dto.LoginRequest;
import com.study.studyproject.login.dto.TokenDtoResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@NoArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 86400)
public class RefreshToken {
    @Id
    private String email;

    @Indexed
    private String accessToken;

    private String refreshToken;


    @Builder
    public RefreshToken(String email, String accessToken,String refreshToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
        this.email = email;
    }


    public static RefreshToken toEntity(TokenDtoResponse tokensDto, LoginRequest loginRequest) {
        return RefreshToken.builder()
                .refreshToken(tokensDto.getRefreshToken())
                .accessToken(tokensDto.getAccessToken())
                .email(loginRequest.getEmail()).build();
    }


    public RefreshToken updateToken(String token) {
        this.refreshToken = token;
        return this;
    }

}