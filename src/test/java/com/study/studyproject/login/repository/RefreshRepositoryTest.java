package com.study.studyproject.login.repository;

import com.study.studyproject.login.domain.RefreshToken;
import com.study.studyproject.member.domain.Email;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RefreshRepositoryTest {
    @Autowired
    private RefreshRepository refreshRepository;
    final String Refresh_Token = "refresh";
    final String Access_Token = "access";
    final String email = "jac";


    @BeforeEach
    void shutDown() {
        refreshRepository.save(refreshToken());
    }

    @AfterEach
    void tearDown() {
        refreshRepository.delete(refreshToken());

    }

    @Test
    @DisplayName("redis에서 데이터를 저장하여 조회한다.")
    void saveAndFindTest() throws Exception {
        //given
        Optional<RefreshToken> findRefreshToken = refreshRepository.findByAccessToken(Access_Token);


        //then
        assertThat(Refresh_Token).isEqualTo(findRefreshToken.get().getRefreshToken());

    }

    @Test
    @DisplayName("redis에서 데이터를 저장하여 삭제한다")
    void deleteTest() throws Exception {
        //given
        Optional<RefreshToken> findRefreshToken = refreshRepository.findByAccessToken(Access_Token);


        //then
        refreshRepository.delete(findRefreshToken.get());


        Optional<RefreshToken> res = refreshRepository.findByAccessToken(Access_Token);
        assertThat(res).isEmpty();

    }






    private RefreshToken refreshToken
            () {
        {
            return RefreshToken.builder()
                    .refreshToken(Refresh_Token)
                    .accessToken(Access_Token)
                    .email(new Email("jac"))
                    .build();
        }

    }

}