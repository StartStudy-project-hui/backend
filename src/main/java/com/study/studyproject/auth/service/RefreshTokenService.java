package com.study.studyproject.auth.service;

import com.study.studyproject.auth.domain.RefreshToken;
import com.study.studyproject.auth.dto.TokenDtoResponse;
import com.study.studyproject.auth.repository.RefreshRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshRepository refreshRepository;

    public void saveOrUpdate(String email, TokenDtoResponse tokensDto) {
        // RefreshToken의 @Id는 email이므로 save()만으로 기존 값을 그대로 덮어써 upsert된다.
        // 매번 새로 발급되는 accessToken으로 findByAccessToken을 먼저 조회하던 기존 로직은
        // 방금 생성한 토큰이라 항상 조회 결과가 없어 불필요한 Redis 조회만 발생시켰다.
        refreshRepository.save(RefreshToken.toEntity(tokensDto, email));
    }

}
