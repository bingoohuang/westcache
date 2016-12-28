package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.manager.RedisCacheManager;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public class RedisManagerTest {
    static Jedis jedis;

    @BeforeClass
    public static void beforeClass() {
        jedis = new Jedis("127.0.0.1", 7379);
        WestCacheRegistry.register("redis",
                new RedisCacheManager(jedis));
    }

    @AfterClass
    public static void afterClass() {
        WestCacheRegistry.deregisterManager("redis");
        jedis.close();
    }

    interface RedisManagerService {
        @WestCacheable(manager = "redis", keyer = "simple")
        String getSomething();
    }

    @Test
    public void test() {
        val service = WestCacheFactory.create(RedisManagerService.class);
        jedis.set("westcache:RedisManagerTest.RedisManagerService.getSomething", "\"bingoo\"");

        String something = service.getSomething();
        assertThat(something).isEqualTo("bingoo");
    }

}
