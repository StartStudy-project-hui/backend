package com.study.studyproject.login.email.service;

import com.study.studyproject.global.exception.ex.BadRequestException;
import com.study.studyproject.global.hash.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

import static com.study.studyproject.global.exception.ex.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    public static final String CODE_KEY_PREFIX = "email:verification:code:";
    public static final String VERIFIED_KEY_PREFIX = "email:verification:verified:";
    public static final String COOLDOWN_KEY_PREFIX = "email:verification:cooldown:";

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);
    private static final Duration COOLDOWN_TTL = Duration.ofSeconds(60);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender mailSender;

    public void sendCode(String email) {
        String hash = HashUtil.sha256(email);
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();

        if (ops.get(COOLDOWN_KEY_PREFIX + hash) != null) {
            throw new BadRequestException(EMAIL_CODE_COOLDOWN);
        }

        String code = generateCode();
        sendMail(email, code);

        ops.set(CODE_KEY_PREFIX + hash, code, CODE_TTL);
        ops.set(COOLDOWN_KEY_PREFIX + hash, "true", COOLDOWN_TTL);
    }

    public void verifyCode(String email, String code) {
        String hash = HashUtil.sha256(email);
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();

        Object stored = ops.get(CODE_KEY_PREFIX + hash);
        if (stored == null) {
            throw new BadRequestException(EMAIL_CODE_NOT_FOUND);
        }
        if (!stored.equals(code)) {
            throw new BadRequestException(EMAIL_CODE_MISMATCH);
        }

        redisTemplate.delete(CODE_KEY_PREFIX + hash);
        ops.set(VERIFIED_KEY_PREFIX + hash, "true", VERIFIED_TTL);
    }

    public boolean isVerified(String email) {
        String hash = HashUtil.sha256(email);
        return redisTemplate.opsForValue().get(VERIFIED_KEY_PREFIX + hash) != null;
    }

    public void clearVerification(String email) {
        String hash = HashUtil.sha256(email);
        redisTemplate.delete(VERIFIED_KEY_PREFIX + hash);
    }

    private String generateCode() {
        int code = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    private void sendMail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[Start Study] 이메일 인증번호");
        message.setText("인증번호는 [" + code + "] 입니다. 5분 이내에 입력해주세요.");
        mailSender.send(message);
    }
}
