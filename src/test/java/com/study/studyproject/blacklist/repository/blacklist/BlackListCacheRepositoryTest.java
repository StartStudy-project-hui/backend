package com.study.studyproject.blacklist.repository.blacklist;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BlackListCacheRepositoryTest {

    private static final String HASH = "blacklist-cache-repo-test-hash";

    @Autowired
    private BlackListCacheRepository blackListCacheRepository;

    @AfterEach
    void tearDown() {
        blackListCacheRepository.evict(HASH);
    }

    @Test
    @DisplayName("차단 여부를 저장하면 조회된다")
    void saveAndFindBlocked() {
        //when
        blackListCacheRepository.saveBlocked(HASH, true);

        //then
        assertThat(blackListCacheRepository.findBlocked(HASH)).isTrue();
    }

    @Test
    @DisplayName("저장된 적 없는 캐시를 조회하면 null이다")
    void findBlockedWhenNotCached() {
        assertThat(blackListCacheRepository.findBlocked(HASH)).isNull();
    }

    @Test
    @DisplayName("차단 아님 상태도 정확히 저장/조회된다")
    void saveAndFindNotBlocked() {
        //when
        blackListCacheRepository.saveBlocked(HASH, false);

        //then
        assertThat(blackListCacheRepository.findBlocked(HASH)).isFalse();
    }

    @Test
    @DisplayName("캐시를 지우면 더 이상 조회되지 않는다")
    void evict() {
        //given
        blackListCacheRepository.saveBlocked(HASH, true);

        //when
        blackListCacheRepository.evict(HASH);

        //then
        assertThat(blackListCacheRepository.findBlocked(HASH)).isNull();
    }
}
