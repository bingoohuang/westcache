package com.github.bingoohuang.westcache;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/2.
 */
public class ExpiringMapCacheManagerTest {
    public static class ExpiringDemoService {
        @Setter private long timestamp;

        @WestCacheable(manager = "expiring", specs = "expireAfterAccess=1s")
        public String cacheMe() {
            return "Access@" + timestamp;
        }

        @WestCacheable(manager = "expiring", specs = "expireAfterWrite=1s")
        public String cacheHer() {
            return "Write@" + timestamp;
        }
    }

    @Test @SneakyThrows
    public void expiringAfterAccess() {
        val service = WestCacheFactory.create(ExpiringDemoService.class);
        service.setTimestamp(1L);

        val cacheValue1 = service.cacheMe();
        assertThat(cacheValue1).isEqualTo("Access@1");
        service.setTimestamp(3L);
        val cacheValue2 = service.cacheMe();
        assertThat(cacheValue1).isSameAs(cacheValue2);

        Thread.sleep(1000L);
        val cacheValue3 = service.cacheMe();
        assertThat(cacheValue3).isEqualTo("Access@3");
        val cacheValue4 = service.cacheMe();
        assertThat(cacheValue3).isSameAs(cacheValue4);
    }

    @Test @SneakyThrows
    public void expiringAfterWrite() {
        val service = WestCacheFactory.create(ExpiringDemoService.class);
        service.setTimestamp(1L);

        val cacheValue1 = service.cacheHer();
        assertThat(cacheValue1).isEqualTo("Write@1");
        Thread.sleep(500L);
        service.setTimestamp(3L);
        val cacheValue2 = service.cacheHer();
        assertThat(cacheValue1).isSameAs(cacheValue2);

        Thread.sleep(500L);
        val cacheValue3 = service.cacheHer();
        assertThat(cacheValue3).isEqualTo("Write@3");
        val cacheValue4 = service.cacheHer();
        assertThat(cacheValue3).isSameAs(cacheValue4);
    }
}
