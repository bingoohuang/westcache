package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheable;
import com.github.bingoohuang.westcache.flusher.SimpleCacheFlusher;
import com.github.bingoohuang.westcache.impl.WestCacheOption;
import com.github.bingoohuang.westcache.snapshot.FileCacheSnapshot;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.github.bingoohuang.westcache.utils.CacheKeyUtils.createCacheKey;
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

    static SimpleCacheFlusher flusher = new SimpleCacheFlusher();
    static WestCacheOption option1 = new WestCacheOption(false, flusher);
    static WestCacheOption option2 = new WestCacheOption(true, flusher);

    @BeforeClass
    public static void beforeClass() {
        WestCacheFlusherManager.registerFlusher("simple", flusher);
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

        WestCacheFlusherManager.flush(option1, bean, "getHomeAreaWithCache");
        cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(south);
    }

    public static class FlushSnapshotBean {
        @Getter @Setter String homeArea;

        @WestCacheable(snapshot = true, flusher = "simple") @SneakyThrows
        public String getHomeAreaWithCache() {
            // 700 milliseconds to simulate slow of reading big data
            Thread.sleep(700L);
            System.out.println("FlushSnapshotBean executed");
            return homeArea;
        }
    }

    @Test @SneakyThrows
    public void flushSnapshot() {
        val bigDataXXX = "SnapshotService.getBigData.XXX";

        val methodName = "getHomeAreaWithCache";
        val cacheKey = createCacheKey(FlushSnapshotBean.class, methodName);

        val snapshot = new FileCacheSnapshot();
        snapshot.saveSnapshot(cacheKey, bigDataXXX);

        FlushSnapshotBean bean = WestCacheFactory.create(FlushSnapshotBean.class);

        bean.setHomeArea(north);
        long start = System.currentTimeMillis();
        String cached = bean.getHomeAreaWithCache();
        long cost = System.currentTimeMillis() - start;

        assertThat(cost).isLessThan(700L);
        assertThat(cached).isEqualTo(bigDataXXX);
        Thread.sleep(700L - cost + 100);

        cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(north);

        bean.setHomeArea(south);
        cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(north);

        WestCacheFlusherManager.flush(option2, bean, "getHomeAreaWithCache");
        bean.getHomeAreaWithCache();
        Thread.sleep(700L);

        cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(south);
    }
}
