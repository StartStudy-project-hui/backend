package com.study.studyproject.global.config.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class
RedisUtils {
    private final RedisTemplate<String, Object> redisTemplate;

    public void setValue(String key, String data) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, data);
    }


    public void setValue(String key, String data, Duration duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, data, duration);
    }

    @Transactional(readOnly = true)
    public String getValue(String key) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        Object value = values.get(key);
        return value != null ? (String) value : null;
    }

    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

    public boolean setIfAbsent(String key, String data, Duration duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        Boolean acquired = values.setIfAbsent(key, data, duration);
        return Boolean.TRUE.equals(acquired);
    }

    public void expireValues(String key, int timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
    }

    public void setHashValue(String key, Map<String, String> data) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        values.putAll(key, data);
    }

    @Transactional(readOnly = true)
    public String getHashValue(String key, String hashKey) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        Object value = values.get(key, hashKey);
        return value != null ? (String) value : "";
    }

    public void deleteHashValue(String key, String hashKey) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        values.delete(key, hashKey);
    }

}
