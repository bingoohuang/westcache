package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.snapshot.RedisCacheSnapshot;
import com.github.bingoohuang.westcache.utils.WestCacheOptions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import static com.github.bingoohuang.westcache.WestCacheRegistry.*;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
public class RedisSnapshotTest {
    static Jedis jedis;

    @BeforeClass
    public static void beforeClass() {
        jedis = new Jedis("127.0.0.1", 7379);
        registerSnapshot("redisSnapshot",
                new RedisCacheSnapshot(jedis));
    }

    @AfterClass
    public static void afterClass() {
        deregisterSnapshot("redisSnapshot");
        jedis.close();
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class XyzBean {
        private int id;
        private String name, addr;
    }

    static volatile int cacheMethodExecutedTimes = 0;

    public static class XyzServie {
        @WestCacheable(snapshot = "redisSnapshot")
        public XyzBean getXyzBean(int id) {
            ++cacheMethodExecutedTimes;
            return id == 1
                    ? new XyzBean(1, "bingoo", "南京 ")
                    : new XyzBean(2, "dingoo", "北京");
        }
    }

    @Test
    public void redisSnapshot() {
        val service = WestCacheFactory.create(XyzServie.class);

        val snapshot = getSnapshot("redisSnapshot");

        val keyStrategy = WestCacheRegistry.getKeyStrategy("default");
        val option = WestCacheOptions.newBuilder()
                .snapshot("redisSnapshot").build();
        val cacheKey1 = keyStrategy.getCacheKey(option, "getXyzBean", service, "1");
        val cacheKey2 = keyStrategy.getCacheKey(option, "getXyzBean", service, "2");


        snapshot.deleteSnapshot(cacheKey1);
        snapshot.deleteSnapshot(cacheKey2);

        val bean1 = service.getXyzBean(1);

        assertThat(cacheMethodExecutedTimes).isEqualTo(1);
        assertThat(bean1).isEqualTo(new XyzBean(1, "bingoo", "南京 "));

        val bean2 = service.getXyzBean(1);
        assertThat(cacheMethodExecutedTimes).isEqualTo(1);
        assertThat(bean2).isSameAs(bean1);

        val bean3 = service.getXyzBean(2);
        assertThat(cacheMethodExecutedTimes).isEqualTo(2);
        assertThat(bean3).isEqualTo(new XyzBean(2, "dingoo", "北京"));



        val cached = snapshot.readSnapshot(cacheKey1);
        assertThat(cached.isPresent()).isTrue();
        assertThat(cached.get()).isEqualTo(new XyzBean(1, "bingoo", "南京 "));
    }
}
