package com.github.bingoohuang.westcache.utils;

import lombok.val;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/24.
 */
public class RedisTest {
    @Test
    public void waitRedisLock() {
        val jedis = Redis.getJedis();
        jedis.setnx("redis.test", "lock");
        val locked = Redis.waitRedisLock(jedis, "redis.test");
        assertThat(locked).isFalse();
    }
}
