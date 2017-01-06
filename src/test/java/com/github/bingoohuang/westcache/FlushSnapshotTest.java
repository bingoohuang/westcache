package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.github.bingoohuang.westcache.WestCacheRegistry.*;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/5.
 */
public class FlushSnapshotTest {
    public static class FlushSnapshotBean {
        @Getter @Setter volatile String homeArea;
        @Setter volatile long sleepMillis = 150L;
        @Getter @Setter volatile boolean cacheMethodExecuted = false;

        @WestCacheable(snapshot = "file", flusher = "simple",
                config = "snapshotTest") @SneakyThrows
        public String getHomeAreaWithCache() {
            // 700 milliseconds to simulate slow of reading big data
            Thread.sleep(sleepMillis);
            setCacheMethodExecuted(true);
            return homeArea;
        }
    }

    @BeforeClass
    public static void beforeClass() {
        configRegistry.register("snapshotTest",
                new DefaultWestCacheConfig() {
                    @Override public long timeoutMillisToSnapshot() {
                        return 100L;
                    }
                });
    }

    @AfterClass
    public static void afterClass() {
        configRegistry.deregister("snapshotTest");
    }

    String north = "NORTH", south = "SOUTH";

    @Test @SneakyThrows
    public void flushSnapshot() {
        val bean = WestCacheFactory.create(FlushSnapshotBean.class);

        val bigDataXXX = "BigData.XXX";

        val snapshot = snapshotRegistry.get("file");
        val keyer = keyerRegistry.get("default");
        val option = WestCacheOption.newBuilder()
                .snapshot("file").flusher("simple").config("snapshotTest")
                .build();
        val cacheKey = keyer.getCacheKey(option, "getHomeAreaWithCache", bean);
        snapshot.saveSnapshot(option, cacheKey, new WestCacheItem(bigDataXXX));


        bean.setHomeArea(north);
        bean.setCacheMethodExecuted(false);
        String cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(bigDataXXX);

        do {
            Thread.sleep(50L);
        } while (!bean.isCacheMethodExecuted());

        cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(north);

        bean.setHomeArea(south);
        cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(north);

        WestCacheRegistry.flush(option, bean, "getHomeAreaWithCache");
        bean.setSleepMillis(0L);
        cached = bean.getHomeAreaWithCache();

        assertThat(cached).isEqualTo(south);
    }
}
