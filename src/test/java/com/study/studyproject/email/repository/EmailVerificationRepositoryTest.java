package com.study.studyproject.email.repository;

import com.study.studyproject.global.config.redis.RedisUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EmailVerificationRepositoryTest {

    private static final String HASH = "email-repo-test-hash";
    private static final Duration TTL = Duration.ofMinutes(5);

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private RedisUtils redisUtils;

    @AfterEach
    void tearDown() {
        emailVerificationRepository.deleteCode(HASH);
        emailVerificationRepository.deleteVerified(HASH);
        redisUtils.deleteValue(EmailVerificationRepository.COOLDOWN_KEY_PREFIX + HASH);
    }

    @Test
    @DisplayName("인증번호를 저장하면 조회된다")
    void saveAndFindCode() {
        //when
        emailVerificationRepository.saveCode(HASH, "123456", TTL);

        //then
        assertThat(emailVerificationRepository.findCode(HASH)).isEqualTo("123456");
    }

    @Test
    @DisplayName("저장된 적 없는 인증번호를 조회하면 null이다")
    void findCodeWhenNotSaved() {
        assertThat(emailVerificationRepository.findCode(HASH)).isNull();
    }

    @Test
    @DisplayName("인증번호를 삭제하면 더 이상 조회되지 않는다")
    void deleteCode() {
        //given
        emailVerificationRepository.saveCode(HASH, "123456", TTL);

        //when
        emailVerificationRepository.deleteCode(HASH);

        //then
        assertThat(emailVerificationRepository.findCode(HASH)).isNull();
    }

    @Test
    @DisplayName("쿨다운이 걸려있지 않으면 시작에 성공하고, 이미 걸려있으면 실패한다")
    void tryStartCooldown() {
        //when
        boolean first = emailVerificationRepository.tryStartCooldown(HASH, TTL);
        boolean second = emailVerificationRepository.tryStartCooldown(HASH, TTL);

        //then
        assertThat(first).isTrue();
        assertThat(second).isFalse();
    }

    @Test
    @DisplayName("인증완료 처리를 하면 isVerified가 true를 반환한다")
    void markVerified() {
        //when
        emailVerificationRepository.markVerified(HASH, TTL);

        //then
        assertThat(emailVerificationRepository.isVerified(HASH)).isTrue();
    }

    @Test
    @DisplayName("인증완료 처리를 한 적 없으면 isVerified는 false를 반환한다")
    void isVerifiedWhenNotMarked() {
        assertThat(emailVerificationRepository.isVerified(HASH)).isFalse();
    }

    @Test
    @DisplayName("인증완료 상태를 삭제하면 isVerified는 다시 false를 반환한다")
    void deleteVerified() {
        //given
        emailVerificationRepository.markVerified(HASH, TTL);

        //when
        emailVerificationRepository.deleteVerified(HASH);

        //then
        assertThat(emailVerificationRepository.isVerified(HASH)).isFalse();
    }
}
