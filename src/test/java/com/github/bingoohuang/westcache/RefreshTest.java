package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheable;
import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.snapshot.FileCacheSnapshot;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.github.bingoohuang.westcache.WestCacheOptions.newBuilder;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class RefreshTest {
    String north = "NORTH", south = "SOUTH";

    public static class FlushBean {
        @Getter @Setter String homeArea;

        @WestCacheable(flusher = "simple")
        public String getHomeAreaWithCache() {
            return homeArea;
        }
    }

    @Test
    public void flush() {
        FlushBean bean = WestCacheFactory.create(FlushBean.class);

        bean.setHomeArea(north);
        String cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(north);

        bean.setHomeArea(south);
        cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(north);

        val option1 = newBuilder().build();
        WestCacheRegistry.flush(option1, bean, "getHomeAreaWithCache");
        cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(south);
    }

    public static class FlushSnapshotBean {
        @Getter @Setter String homeArea;
        @Setter long sleepMillis = 150L;

        @WestCacheable(snapshot = "file", flusher = "simple",
                config = "snapshotTestConfig") @SneakyThrows
        public String getHomeAreaWithCache() {
            // 700 milliseconds to simulate slow of reading big data
            Thread.sleep(sleepMillis);
            System.out.println("FlushSnapshotBean executed");
            return homeArea;
        }
    }

    @BeforeClass
    public static void beforeClass() {
        WestCacheRegistry.registerConfig("snapshotTestConfig",
                new DefaultWestCacheConfig() {
                    @Override public long timeoutMillisToSnapshot() {
                        return 100L;
                    }
                });
    }

    @AfterClass
    public static void afterClass() {
        WestCacheRegistry.deregisterConfig("snapshotTestConfig");
    }

    @Test @SneakyThrows
    public void flushSnapshot() {
        val bigDataXXX = "SnapshotService.getBigData.XXX";

        val snapshot = new FileCacheSnapshot();
        val cacheKey = FlushSnapshotBean.class.getName() + ".getHomeAreaWithCache";
        snapshot.saveSnapshot(cacheKey, bigDataXXX);

        val bean = WestCacheFactory.create(FlushSnapshotBean.class);

        bean.setHomeArea(north);
        long start = System.currentTimeMillis();
        String cached = bean.getHomeAreaWithCache();
        long cost = System.currentTimeMillis() - start;

        assertThat(cost).isLessThan(500L);
        assertThat(cached).isEqualTo(bigDataXXX);
        Thread.sleep(250L - cost);

        cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(north);

        bean.setHomeArea(south);
        cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(north);

        val option2 = newBuilder().snapshot("file").build();
        WestCacheRegistry.flush(option2, bean, "getHomeAreaWithCache");
        bean.setSleepMillis(0L);
        cached = bean.getHomeAreaWithCache();

        assertThat(cached).isEqualTo(south);
    }
}
