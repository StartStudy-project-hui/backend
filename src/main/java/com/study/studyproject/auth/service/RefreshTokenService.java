package com.study.studyproject.login.service;

import com.study.studyproject.login.domain.RefreshToken;
import com.study.studyproject.login.dto.TokenDtoResponse;
import com.study.studyproject.login.repository.RefreshRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshRepository refreshRepository;

    public void saveOrUpdate(String email, TokenDtoResponse tokensDto) {
        refreshRepository.findByAccessToken(tokensDto.getAccessToken())
                .ifPresentOrElse(
                        token -> refreshRepository.save(token.updateToken(tokensDto.getRefreshToken())), // 존재한다면
                        () -> refreshRepository.save(RefreshToken.toEntity(tokensDto, email)) // 존재하지 않으면
                );
    }

}
