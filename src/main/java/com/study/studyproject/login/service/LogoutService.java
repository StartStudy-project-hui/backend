package com.study.studyproject.login.service;

import com.study.studyproject.login.domain.RefreshToken;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.login.repository.RefreshRepository;
import com.study.studyproject.member.domain.Email;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LogoutService {

    private final RefreshRepository refreshRepository;


    //로그아웃
    public GlobalResultDto logoutService(String token) {
        Optional<RefreshToken> findERefreshToken = refreshRepository.findByAccessToken(token);
        if (findERefreshToken.isPresent()) {
            refreshRepository.delete(findERefreshToken.get());
        }
        return new GlobalResultDto("로그아웃 되었습니다.", HttpStatus.OK.value());
    }
}
