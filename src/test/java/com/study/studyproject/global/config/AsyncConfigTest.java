package com.study.studyproject.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncConfigTest {

    private final AsyncConfig asyncConfig = new AsyncConfig();

    @Test
    @DisplayName("mailExecutor 빈은 지정한 풀 크기/큐 용량/스레드 이름 접두사로 생성된다")
    void mailExecutorHasConfiguredPoolSettings() {
        // when
        Executor executor = asyncConfig.mailExecutor();

        // then
        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;

        assertThat(taskExecutor.getCorePoolSize()).isEqualTo(4);
        assertThat(taskExecutor.getMaxPoolSize()).isEqualTo(10);
        assertThat(taskExecutor.getThreadNamePrefix()).isEqualTo("mailExecutor-");
        // 빈 큐 상태에서는 remainingCapacity가 곧 설정된 queueCapacity와 같다
        assertThat(taskExecutor.getThreadPoolExecutor().getQueue().remainingCapacity()).isEqualTo(50);
    }

    @Test
    @DisplayName("mailExecutor는 큐가 가득 차면 CallerRunsPolicy로 호출 스레드가 직접 실행한다")
    void mailExecutorUsesCallerRunsPolicyOnRejection() {
        // when
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) asyncConfig.mailExecutor();

        // then
        assertThat(taskExecutor.getThreadPoolExecutor().getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);
    }
}
