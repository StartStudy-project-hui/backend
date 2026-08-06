package com.study.studyproject.global.config.redis;

import jakarta.transaction.Transactional;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Key;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisUtilsTest {

    final String KEY = "key";
    final String VALUE = "value";
    final Duration DURATION = Duration.ofMillis(5000);
    final String HASH_PARENT_KEY = "hashParentKey";
    final String HASH_KEY = "hashKey";
    final String HASH_VALUE = "hashValue";

    @Autowired
    private RedisUtils redisUtils;

    @BeforeEach
    void shutDown() {
        redisUtils.setValues(KEY, VALUE, DURATION);
    }

    @AfterEach
    void tearDown() {
        redisUtils.deleteValues(KEY);
        redisUtils.deleteHashOps(HASH_PARENT_KEY, HASH_KEY);
    }


    @Test
    @DisplayName("Redis에 데이터를 저장하면 조회된다")
    void saveAndFindTest() throws Exception {
        //given
        String findValue = redisUtils.getValues(KEY);

        //then
        assertThat(VALUE).isEqualTo(findValue);


    }

    @Test
    @DisplayName("Redis에 저장된 데이터를 수정한다.")
    void updateTest() throws Exception {
        //given
        String updateValue = "updateValue";
        redisUtils.setValues(KEY, updateValue, DURATION);

        //when
        String findValue = redisUtils.getValues(KEY);

        //then
        assertThat(updateValue).isEqualTo(findValue);
        assertThat(VALUE).isNotEqualTo(findValue);
    }

    @Test
    @DisplayName("Redis에 저장된 데이터를 삭제한다.")
    void deleteTest() throws Exception {
        //given
        redisUtils.deleteValues(KEY);
        String findValue = redisUtils.getValues(KEY);

        //then
        assertThat(findValue).isNull();
    }

    @Test
    @DisplayName("Redis에 저장된 데이터는 만료시간이 지나면 삭제된다.")
    void expiredTest() throws Exception {
        String findValue = redisUtils.getValues(KEY);
        Awaitility.await().pollDelay(Duration.ofMillis(6000)).untilAsserted(
                () -> {
                    String expiredValue = redisUtils.getValues(KEY);
                    assertThat(expiredValue).isNotEqualTo(findValue);
                    assertThat(expiredValue).isNull();
                }
        );

    }

    @Test
    @DisplayName("setIfAbsent는 키가 없을 때만 값을 저장하고 true를 반환한다.")
    void setIfAbsentTest() throws Exception {
        //given
        String newKey = "setIfAbsentKey";
        redisUtils.deleteValues(newKey);

        try {
            //when
            boolean firstAcquired = redisUtils.setIfAbsent(newKey, VALUE, DURATION);
            boolean secondAcquired = redisUtils.setIfAbsent(newKey, "otherValue", DURATION);

            //then
            assertThat(firstAcquired).isTrue();
            assertThat(secondAcquired).isFalse();
            assertThat(redisUtils.getValues(newKey)).isEqualTo(VALUE);
        } finally {
            redisUtils.deleteValues(newKey);
        }
    }

    @Test
    @DisplayName("Redis 해시에 저장된 데이터를 hashKey로 조회하면 값이 반환된다.")
    void getHashOpsTest() throws Exception {
        //given
        redisUtils.setHashOps(HASH_PARENT_KEY, Map.of(HASH_KEY, HASH_VALUE));

        //when
        String findValue = redisUtils.getHashOps(HASH_PARENT_KEY, HASH_KEY);

        //then
        assertThat(findValue).isEqualTo(HASH_VALUE);
    }

    @Test
    @DisplayName("존재하지 않는 hashKey로 조회하면 빈 문자열이 반환된다.")
    void getHashOpsWhenHashKeyNotExistsTest() throws Exception {
        //given
        redisUtils.setHashOps(HASH_PARENT_KEY, Map.of(HASH_KEY, HASH_VALUE));

        //when
        String findValue = redisUtils.getHashOps(HASH_PARENT_KEY, "notExistsHashKey");

        //then
        assertThat(findValue).isEqualTo("");
    }

    @Test
    @DisplayName("존재하지 않는 key로 조회하면 빈 문자열이 반환된다.")
    void getHashOpsWhenKeyNotExistsTest() throws Exception {
        //when
        String findValue = redisUtils.getHashOps("notExistsParentKey", HASH_KEY);

        //then
        assertThat(findValue).isEqualTo("");
    }

    @Test
    @DisplayName("Redis 해시에서 hashKey를 삭제하면 더 이상 조회되지 않는다.")
    void deleteHashOpsTest() throws Exception {
        //given
        redisUtils.setHashOps(HASH_PARENT_KEY, Map.of(HASH_KEY, HASH_VALUE));

        //when
        redisUtils.deleteHashOps(HASH_PARENT_KEY, HASH_KEY);

        //then
        assertThat(redisUtils.getHashOps(HASH_PARENT_KEY, HASH_KEY)).isEqualTo("");
    }






}