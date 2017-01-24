package com.github.bingoohuang.westcache.utils;

import org.junit.Test;
import redis.clients.jedis.JedisCommands;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/24.
 */
public class RedisTest {
    @Test
    public void waitRedisLock() {
        JedisCommands jedis = Redis.getJedis();
        jedis.setnx("redis.test", "lock");
        boolean ok = Redis.waitRedisLock(jedis, "redis.test");
        assertThat(ok).isFalse();
    }
}
