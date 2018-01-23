package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.utils.Envs;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
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

        @WestCacheable(snapshot = "file", flusher = "simple", config = "snapshotTest")
        public String getHomeAreaWithCache() {
            // 700 milliseconds to simulate slow of reading big data
            Envs.sleepMillis(sleepMillis);
            setCacheMethodExecuted(true);
            return homeArea;
        }
    }

    @BeforeClass
    public static void beforeClass() {
        REGISTRY_TEMPLATE.register("snapshotTest",
                new DefaultWestCacheConfig() {
                    @Override public long timeoutMillisToSnapshot() {
                        return 100L;
                    }
                });
    }

    @AfterClass
    public static void afterClass() {
        REGISTRY_TEMPLATE.deregister("snapshotTest");
    }

    String north = "NORTH", south = "SOUTH";

    @Test
    public void flushSnapshot() {
        val bean = WestCacheFactory.create(FlushSnapshotBean.class);

        val bigDataXXX = "BigData.XXX";

        val snapshot = SNAPSHOT_REGISTRY.get("file");
        val keyer = KEYER_REGISTRY.get("default");
        val option = WestCacheOption.builder()
                .snapshot("file").flusher("simple").config("snapshotTest")
                .build();
        val cacheKey = keyer.getCacheKey(option, "getHomeAreaWithCache", bean);
        snapshot.saveSnapshot(option, cacheKey, new WestCacheItem(Optional.fromNullable(bigDataXXX), option));


        bean.setHomeArea(north);
        bean.setCacheMethodExecuted(false);
        String cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(bigDataXXX);

        do {
            Envs.sleepMillis(50L);
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
