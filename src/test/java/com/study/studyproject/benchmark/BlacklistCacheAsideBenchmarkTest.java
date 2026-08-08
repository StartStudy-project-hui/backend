package com.study.studyproject.benchmark;

import com.study.studyproject.blacklist.domain.BlackList;
import com.study.studyproject.blacklist.repository.blacklist.BlackListCacheRepository;
import com.study.studyproject.blacklist.repository.blacklist.BlackListRepository;
import com.study.studyproject.blacklist.service.BlackListService;
import com.study.studyproject.global.hash.HashUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

/**
 * 캐시-어사이드 도입 효과를 실측하기 위한 1회성 벤치마크.
 * ./gradlew test --tests "*BlacklistCacheAsideBenchmarkTest*" 로 단독 실행.
 */
@SpringBootTest
class BlacklistCacheAsideBenchmarkTest {

    private static final String EMAIL = "benchmark-blacklist@startstudy.local";
    private static final int WARMUP = 50;
    private static final int ITERATIONS = 500;

    @Autowired
    private BlackListService blackListService;
    @Autowired
    private BlackListRepository blackListRepository;
    @Autowired
    private BlackListCacheRepository blackListCacheRepository;

    private String hash;

    @AfterEach
    void tearDown() {
        blackListCacheRepository.evict(hash);
        blackListRepository.findByHashValue(hash).ifPresent(blackListRepository::delete);
    }

    @Test
    @DisplayName("isBlocked() 캐시 히트 vs DB 폴백 지연시간 비교")
    void compareCacheHitVsDbFallback() {
        hash = HashUtil.sha256(EMAIL);
        BlackList blackList = BlackList.create(hash, "benchmark");
        blackList.makePermanent();
        blackListRepository.save(blackList);

        // 워밍업 (JIT/커넥션 풀 워밍업, 측정에서 제외)
        for (int i = 0; i < WARMUP; i++) {
            blackListCacheRepository.evict(hash);
            blackListService.isBlocked(EMAIL);
        }

        // 1) DB 폴백 경로: 매 호출 전 캐시 무효화 -> 항상 캐시 미스
        long[] dbLatenciesNs = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            blackListCacheRepository.evict(hash);
            long start = System.nanoTime();
            blackListService.isBlocked(EMAIL);
            dbLatenciesNs[i] = System.nanoTime() - start;
        }

        // 2) 캐시 히트 경로: 한 번만 채워두고 이후 전부 히트
        blackListCacheRepository.evict(hash);
        blackListService.isBlocked(EMAIL); // 캐시 채우기
        long[] cacheLatenciesNs = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            blackListService.isBlocked(EMAIL);
            cacheLatenciesNs[i] = System.nanoTime() - start;
        }

        printReport("DB 폴백 (캐시 미스)", dbLatenciesNs);
        printReport("Redis 캐시 히트", cacheLatenciesNs);

        double dbAvgMs = average(dbLatenciesNs) / 1_000_000.0;
        double cacheAvgMs = average(cacheLatenciesNs) / 1_000_000.0;
        double reductionPct = (1 - cacheAvgMs / dbAvgMs) * 100;
        System.out.printf(
                "%n[결과 요약] DB 평균 %.3fms -> 캐시 평균 %.3fms, 지연시간 %.1f%% 감소, %.1fx 처리량%n",
                dbAvgMs, cacheAvgMs, reductionPct, dbAvgMs / cacheAvgMs
        );
    }

    private void printReport(String label, long[] latenciesNs) {
        long[] sorted = latenciesNs.clone();
        Arrays.sort(sorted);
        double avgMs = average(sorted) / 1_000_000.0;
        double p50Ms = sorted[(int) (sorted.length * 0.50)] / 1_000_000.0;
        double p95Ms = sorted[(int) (sorted.length * 0.95)] / 1_000_000.0;
        double p99Ms = sorted[(int) (sorted.length * 0.99)] / 1_000_000.0;
        System.out.printf(
                "[%s] n=%d avg=%.3fms p50=%.3fms p95=%.3fms p99=%.3fms%n",
                label, sorted.length, avgMs, p50Ms, p95Ms, p99Ms
        );
    }

    private double average(long[] values) {
        long sum = 0;
        for (long v : values) sum += v;
        return (double) sum / values.length;
    }
}
