package com.study.studyproject.login.domain;

import com.study.studyproject.login.dto.LoginRequest;
import com.study.studyproject.login.dto.TokenDtoResponse;
import com.study.studyproject.member.domain.Email;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(value = "refreshToken", timeToLive = 86400)
public class  RefreshToken {
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

    public RefreshToken updateAccessToken(String token) {
        this.accessToken= token;
        return this;
    }

}