package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.flusher.QuartzCacheFlusher;
import com.github.bingoohuang.westcache.manager.GuavaCacheManager;
import com.github.bingoohuang.westcache.utils.Envs;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.github.bingoohuang.westcache.WestCacheRegistry.FLUSHER_REGISTRY;
import static com.github.bingoohuang.westcache.WestCacheRegistry.MANAGER_REGISTRY;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/16.
 */
public class QuartzFlusherTest {
    public static class QuartzService {
        @WestCacheable(
                manager = "quartz-manager",
                flusher = "quartz-flusher",
                specs = "scheduled=Every 1 second")
        public long doWhat() {
            return System.currentTimeMillis();
        }

        @WestCacheable(
                manager = "quartz-manager",
                flusher = "quartz-flusher")
        public long doThat() {
            return System.currentTimeMillis();
        }
    }

    @BeforeClass
    public static void beforeClass() {
        MANAGER_REGISTRY.register("quartz-manager", new GuavaCacheManager());
        FLUSHER_REGISTRY.register("quartz-flusher", new QuartzCacheFlusher());
    }

    @AfterClass
    public static void afterClass() {
        val flusher = (QuartzCacheFlusher) FLUSHER_REGISTRY.get("quartz-flusher");
        flusher.stopQuartz();

        MANAGER_REGISTRY.deregister("quartz-manager");
        FLUSHER_REGISTRY.deregister("quartz-flusher");
    }

    @Test
    public void test1() {
        val service = WestCacheFactory.create(QuartzService.class);
        long l1 = service.doWhat();
        long l2 = service.doWhat();
        assertThat(l1).isEqualTo(l2);
        Envs.sleepMillis(1000);

        long l3 = service.doWhat();
        assertThat(l3).isGreaterThan(l1);


        long t1 = service.doThat();
        Envs.sleepMillis(1000);
        long t2 = service.doThat();
        assertThat(t1).isEqualTo(t2);
    }
}
