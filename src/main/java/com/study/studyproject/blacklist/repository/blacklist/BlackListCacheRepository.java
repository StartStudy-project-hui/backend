package com.study.studyproject.blacklist.repository.blacklist;

import com.study.studyproject.global.config.redis.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;


@Repository
@RequiredArgsConstructor
public class BlackListCacheRepository {
    public static final String BLACKLIST_CACHE_KEY_PREFIX = "blacklist:status:";

    private static final Duration BLACKLIST_CACHE_TTL = Duration.ofMinutes(5);

    private final RedisUtils redisUtils;

    public Boolean findBlocked(String hash) {
        String cached = redisUtils.getValue(BLACKLIST_CACHE_KEY_PREFIX + hash);
        return cached != null ? Boolean.parseBoolean(cached) : null;
    }

    public void saveBlocked(String hash, boolean blocked) {
        redisUtils.setValue(BLACKLIST_CACHE_KEY_PREFIX + hash, String.valueOf(blocked), BLACKLIST_CACHE_TTL);
    }

    public void evict(String hash) {
        redisUtils.deleteValue(BLACKLIST_CACHE_KEY_PREFIX + hash);
    }
}
