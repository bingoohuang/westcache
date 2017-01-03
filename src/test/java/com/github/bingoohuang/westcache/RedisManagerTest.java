package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.utils.Redis;
import lombok.val;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public class RedisManagerTest {
    interface RedisManagerService {
        @WestCacheable(manager = "redis", keyer = "simple")
        String getSomething();
    }

    @Test
    public void test() {
        val service = WestCacheFactory.create(RedisManagerService.class);
        Redis.jedis.set("westcache:RedisManagerTest" +
                ".RedisManagerService.getSomething", "\"bingoo\"");

        String something = service.getSomething();
        assertThat(something).isEqualTo("bingoo");
    }

}
