package com.study.studyproject.email.repository;

import com.study.studyproject.global.config.redis.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepository {

    public static final String CODE_KEY_PREFIX = "email:verification:code:";
    public static final String VERIFIED_KEY_PREFIX = "email:verification:verified:";
    public static final String COOLDOWN_KEY_PREFIX = "email:verification:cooldown:";

    private final RedisUtils redisUtils;

    public boolean tryStartCooldown(String hash, Duration ttl) {
        return redisUtils.setIfAbsent(COOLDOWN_KEY_PREFIX + hash, "true", ttl);
    }

    public void saveCode(String hash, String code, Duration ttl) {
        redisUtils.setValues(CODE_KEY_PREFIX + hash, code, ttl);
    }

    public String findCode(String hash) {
        return redisUtils.getValues(CODE_KEY_PREFIX + hash);
    }

    public void deleteCode(String hash) {
        redisUtils.deleteValues(CODE_KEY_PREFIX + hash);
    }

    public void markVerified(String hash, Duration ttl) {
        redisUtils.setValues(VERIFIED_KEY_PREFIX + hash, "true", ttl);
    }

    public boolean isVerified(String hash) {
        return redisUtils.getValues(VERIFIED_KEY_PREFIX + hash) != null;
    }

    public void deleteVerified(String hash) {
        redisUtils.deleteValues(VERIFIED_KEY_PREFIX + hash);
    }
}
