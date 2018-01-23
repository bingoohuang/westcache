package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.base.WestCacheKeyer;
import com.github.bingoohuang.westcache.manager.BaseCacheManager;
import com.github.bingoohuang.westcache.utils.Envs;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.Method;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/2.
 */
public class ExpiringCacheManagerTest {
    public static class ExpiringDemoService {
        @Setter private long timestamp;
        @Setter private long timestamp3;
        @Setter private long timestamp4;

        @WestCacheable(manager = "expiring", specs = "expireAfterAccess=1s")
        public String cacheAccess() {
            return "Access@" + timestamp;
        }

        @WestCacheable(manager = "expiring", specs = "expireAfterWrite=1s")
        public String cacheWrite() {
            return "Write@" + timestamp;
        }

        @WestCacheable(manager = "expiring")
        public String cacheThree() {
            return "Three@" + timestamp3;
        }

        @WestCacheable(manager = "expiring")
        public String cacheFour() {
            return "Four@" + timestamp4;
        }
    }

    static ExpiringDemoService service =
            WestCacheFactory.create(ExpiringDemoService.class);


    @Test
    public void put() {
        WestCacheOption option = WestCacheOption.builder().manager("expiring")
                .method(getCacheThreeMethod()).build();
        val manager = (BaseCacheManager) option.getManager();
        WestCacheKeyer keyer = option.getKeyer();
        String cacheKey = keyer.getCacheKey(option, "cacheThree", service);

        manager.getWestCache().put(null, cacheKey, new WestCacheItem(Optional.of("fuck"), option));
        val cacheValue3 = service.cacheThree();
        assertThat(cacheValue3).isEqualTo("fuck");

        manager.getWestCache().invalidate(option, cacheKey, "");
        service.setTimestamp3(10L);
        val cacheValue31 = service.cacheThree();
        assertThat(cacheValue31).isEqualTo("Three@10");
    }

    @SneakyThrows
    private Method getCacheThreeMethod() {
        return ExpiringDemoService.class.getDeclaredMethod("cacheThree");
    }

    @Test @SneakyThrows
    public void expiringAfterAccess() {
        service.setTimestamp(1L);
        val cacheValue1 = service.cacheAccess();
        assertThat(cacheValue1).isEqualTo("Access@1");
        service.setTimestamp(3L);
        val cacheValue2 = service.cacheAccess();
        assertThat(cacheValue1).isSameAs(cacheValue2);

        Envs.sleepMillis(1100L);
        val cacheValue3 = service.cacheAccess();
        assertThat(cacheValue3).isEqualTo("Access@3");
        val cacheValue4 = service.cacheAccess();
        assertThat(cacheValue3).isSameAs(cacheValue4);
    }

    @Test
    public void expiringAfterWrite() {
        service.setTimestamp(1L);
        val cacheValue1 = service.cacheWrite();
        assertThat(cacheValue1).isEqualTo("Write@1");
        Envs.sleepMillis(500L);
        service.setTimestamp(3L);
        val cacheValue2 = service.cacheWrite();
        assertThat(cacheValue1).isSameAs(cacheValue2);

        Envs.sleepMillis(600L);
        val cacheValue3 = service.cacheWrite();
        assertThat(cacheValue3).isEqualTo("Write@3");
        val cacheValue4 = service.cacheWrite();
        assertThat(cacheValue3).isSameAs(cacheValue4);
    }

    @Benchmark
    public void benchmark() {
        service.setTimestamp4(1L);
        val cacheValue3 = service.cacheFour();
        assertThat(cacheValue3).isEqualTo("Four@1");
    }

    @Test @SneakyThrows
    public void bench() {
        Options opt = new OptionsBuilder()
                .include(ExpiringCacheManagerTest.class.getSimpleName())
                .forks(0)
                .warmupIterations(1)
                .measurementIterations(1)
                .threads(12)
                .build();

        new Runner(opt).run();
    }
}
