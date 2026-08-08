package com.study.studyproject.benchmark;

import com.study.studyproject.email.repository.EmailVerificationRepository;
import com.study.studyproject.global.config.redis.RedisUtils;
import com.study.studyproject.global.hash.HashUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 이메일 인증 재발송 쿨다운의 원자적 락(SETNX) 도입 효과를 실측하기 위한 1회성 벤치마크.
 * 동시 요청 상황에서 "정확히 1건만 성공"하는지를, 원자적 구현과 비원자적(get-then-set) 구현으로 비교한다.
 * ./gradlew test --tests "*CooldownAtomicLockBenchmarkTest*" 로 단독 실행.
 */
@SpringBootTest
class CooldownAtomicLockBenchmarkTest {

    private static final String EMAIL = "benchmark-cooldown@startstudy.local";
    private static final Duration TTL = Duration.ofMinutes(1);
    private static final int CONCURRENCY = 50;
    private static final String NAIVE_KEY_PREFIX = "benchmark:naive-cooldown:";

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;
    @Autowired
    private RedisUtils redisUtils;

    private String hash;

    @AfterEach
    void tearDown() {
        hash = HashUtil.sha256(EMAIL);
        redisUtils.deleteValue(EmailVerificationRepository.COOLDOWN_KEY_PREFIX + hash);
        redisUtils.deleteValue(NAIVE_KEY_PREFIX + hash);
    }

    @Test
    @DisplayName("동시 요청 시 원자적 SETNX 락 vs 비원자적 get-then-set 중복 허용률 비교")
    void compareAtomicVsNaiveUnderConcurrency() throws InterruptedException {
        hash = HashUtil.sha256(EMAIL);

        int atomicSuccesses = runConcurrent(() ->
                emailVerificationRepository.tryStartCooldown(hash, TTL));
        redisUtils.deleteValue(EmailVerificationRepository.COOLDOWN_KEY_PREFIX + hash);

        int naiveSuccesses = runConcurrent(() -> naiveTryStartCooldown(hash, TTL));
        redisUtils.deleteValue(NAIVE_KEY_PREFIX + hash);

        System.out.printf(
                "%n[원자적 SETNX] 동시 %d개 요청 중 성공 %d건 (중복 실행률 %.1f%%)%n",
                CONCURRENCY, atomicSuccesses, pctOfExtra(atomicSuccesses)
        );
        System.out.printf(
                "[비원자적 get-then-set] 동시 %d개 요청 중 성공 %d건 (중복 실행률 %.1f%%)%n",
                CONCURRENCY, naiveSuccesses, pctOfExtra(naiveSuccesses)
        );
    }

    private static final int TRIALS = 20;

    @Test
    @DisplayName("트라이얼 20회 반복 - 원자적은 항상 결정론적(분산 0), 비원자적은 실행마다 달라짐을 통계로 검증")
    void repeatedTrialsShowDeterminismVsNonDeterminism() throws InterruptedException {
        hash = HashUtil.sha256(EMAIL);

        int[] atomicResults = new int[TRIALS];
        for (int t = 0; t < TRIALS; t++) {
            // ① 이전 실행의 Redis 잔여 키 삭제 (테스트 환경 초기화)
            redisUtils.deleteValue(EmailVerificationRepository.COOLDOWN_KEY_PREFIX + hash);
            // ② 동시 50개 요청을 보내 성공한 건수를 배열에 저장
            atomicResults[t] = runConcurrent(() -> emailVerificationRepository.tryStartCooldown(hash, TTL));
        }
        // ③ 테스트 종료 후 Redis 깔끔하게 청소
        redisUtils.deleteValue(EmailVerificationRepository.COOLDOWN_KEY_PREFIX + hash);

        int[] naiveResults = new int[TRIALS];
        for (int t = 0; t < TRIALS; t++) {
            redisUtils.deleteValue(NAIVE_KEY_PREFIX + hash);
            naiveResults[t] = runConcurrent(() -> naiveTryStartCooldown(hash, TTL));
        }
        redisUtils.deleteValue(NAIVE_KEY_PREFIX + hash);

        printTrialStats("원자적 SETNX", atomicResults);
        printTrialStats("비원자적 get-then-set", naiveResults);
    }

    private void printTrialStats(String label, int[] results) {
        double mean = Arrays.stream(results).average().orElse(0);
        double variance = Arrays.stream(results).mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0);
        double stdDev = Math.sqrt(variance);
        int min = Arrays.stream(results).min().orElse(0);
        int max = Arrays.stream(results).max().orElse(0);
        System.out.printf(
                "%n[%s] %d회 트라이얼 성공건수: %s%n",
                label, results.length, Arrays.toString(results)
        );
        System.out.printf(
                "[%s] 평균=%.2f 표준편차=%.2f 최소=%d 최대=%d%n",
                label, mean, stdDev, min, max
        );
    }

    /** 원자적 락 없이 흉내낸 구현: get 확인 후 set까지 사이에 경쟁 조건이 생길 수 있는 창을 의도적으로 둔다. */
    private boolean naiveTryStartCooldown(String hash, Duration ttl) {
        String existing = redisUtils.getValue(NAIVE_KEY_PREFIX + hash);
        if (existing != null) {
            return false;
        }
        try {
            Thread.sleep(5); // check-then-act 사이의 경쟁 조건 창(실제 서비스의 네트워크/스케줄링 지연을 흉내)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        redisUtils.setValue(NAIVE_KEY_PREFIX + hash, "true", ttl);
        return true;
    }

    private int runConcurrent(Callable<Boolean> action) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENCY);
        CountDownLatch ready = new CountDownLatch(CONCURRENCY);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < CONCURRENCY; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    if (Boolean.TRUE.equals(action.call())) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        ready.await();       // 모든 스레드가 대기 상태 진입할 때까지 대기
        start.countDown();   // 동시에 출발
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        return successCount.get();
    }

    private double pctOfExtra(int successes) {
        return (successes - 1) * 100.0 / CONCURRENCY;
    }
}
