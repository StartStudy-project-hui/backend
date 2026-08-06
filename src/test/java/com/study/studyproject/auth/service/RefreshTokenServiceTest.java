package com.study.studyproject.auth.service;

import com.study.studyproject.auth.domain.RefreshToken;
import com.study.studyproject.auth.dto.TokenDtoResponse;
import com.study.studyproject.auth.repository.RefreshRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RefreshTokenServiceTest {

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    RefreshRepository refreshRepository;

    final String EMAIL = "jacom2@naver.com";

    @AfterEach
    void tearDown() {
        refreshRepository.deleteById(EMAIL);
    }

    @Test
    @DisplayName("해당 이메일로 저장된 토큰이 없으면, 새로 저장(insert)된다.")
    void saveOrUpdate_insertsWhenNotExists() throws Exception {
        //given
        TokenDtoResponse tokensDto = TokenDtoResponse.of("access-1", "refresh-1");

        //when
        refreshTokenService.saveOrUpdate(EMAIL, tokensDto);

        //then
        Optional<RefreshToken> saved = refreshRepository.findById(EMAIL);
        assertThat(saved).isPresent();
        assertThat(saved.get().getEmail()).isEqualTo(EMAIL);
        assertThat(saved.get().getAccessToken()).isEqualTo("access-1");
        assertThat(saved.get().getRefreshToken()).isEqualTo("refresh-1");
    }

    @Test
    @DisplayName("해당 이메일로 저장된 토큰이 이미 있으면, 기존 값이 새 토큰으로 덮어써진다(upsert).")
    void saveOrUpdate_overwritesWhenExists() throws Exception {
        //given
        refreshTokenService.saveOrUpdate(EMAIL, TokenDtoResponse.of("access-1", "refresh-1"));

        //when
        refreshTokenService.saveOrUpdate(EMAIL, TokenDtoResponse.of("access-2", "refresh-2"));

        //then
        Optional<RefreshToken> saved = refreshRepository.findById(EMAIL);
        assertThat(saved).isPresent();
        assertThat(saved.get().getAccessToken()).isEqualTo("access-2");
        assertThat(saved.get().getRefreshToken()).isEqualTo("refresh-2");

        // 이메일이 @Id이므로 upsert 후에도 레코드는 하나만 존재해야 한다.
        assertThat(refreshRepository.findByAccessToken("access-1")).isEmpty();
    }
}
