package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
public class RedisSnapshotTest {
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class XyzBean {
        private int id;
        private String name, addr;
    }

    static volatile int cacheMethodExecutedTimes = 0;

    public static class XyzServie {
        @WestCacheable(snapshot = "redis")
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
        val snapshot = WestCacheRegistry.getSnapshot("redis");

        val keyStrategy = WestCacheRegistry.getKeyStrategy("default");
        val option = WestCacheOption.newBuilder().snapshot("redis").build();
        val cacheKey1 = keyStrategy.getCacheKey(option, "getXyzBean", service, "1");
        val cacheKey2 = keyStrategy.getCacheKey(option, "getXyzBean", service, "2");

        snapshot.deleteSnapshot(option, cacheKey1);
        snapshot.deleteSnapshot(option, cacheKey2);

        val bean1 = service.getXyzBean(1);

        assertThat(cacheMethodExecutedTimes).isEqualTo(1);
        assertThat(bean1).isEqualTo(new XyzBean(1, "bingoo", "南京 "));

        val bean2 = service.getXyzBean(1);
        assertThat(cacheMethodExecutedTimes).isEqualTo(1);
        assertThat(bean2).isSameAs(bean1);

        val bean3 = service.getXyzBean(2);
        assertThat(cacheMethodExecutedTimes).isEqualTo(2);
        assertThat(bean3).isEqualTo(new XyzBean(2, "dingoo", "北京"));


        val cached = snapshot.readSnapshot(option, cacheKey1);
        assertThat(cached.isPresent()).isTrue();
        assertThat(cached.orNull()).isEqualTo(new XyzBean(1, "bingoo", "南京 "));
    }
}
