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

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class SnapshotTest {
    public static abstract class BasicSnapshotService {
        @Getter @Setter String bigData;
        @Setter long sleepMillis = 150L;

        abstract String getBigDataCache();

        @SneakyThrows
        public String getBigDataSlow() {
            // some milliseconds to simulate slow of reading big data
            Thread.sleep(sleepMillis);
            return bigData;
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

    public static class SnapshotService extends BasicSnapshotService {
        @WestCacheable(snapshot = "file", config = "snapshotTestConfig")
        public String getBigDataCache() {
            return getBigDataSlow();
        }
    }

    public static class SnapshotServiceCustomizedAnnotation extends BasicSnapshotService {
        @WestCacheableSnapshot @SneakyThrows
        public String getBigDataCache() {
            return getBigDataSlow();
        }
    }

    @Test
    public void snapshot() {
        snapshot(SnapshotService.class);
    }

    @Test
    public void customizedAnnotation() {
        snapshot(SnapshotServiceCustomizedAnnotation.class);
    }

    @SneakyThrows
    public void snapshot(Class<? extends BasicSnapshotService> serviceClass) {
        val bigDataXXX = "SnapshotService.getBigData.XXX";
        val bigDataYYY = "SnapshotService.getBigData.YYY";

        val snapshot = new FileCacheSnapshot();
        val cacheKey = serviceClass.getName() + ".getBigDataCache";
        snapshot.saveSnapshot(cacheKey, bigDataXXX);

        val service = WestCacheFactory.create(serviceClass);
        service.setBigData(bigDataYYY);

        long start = System.currentTimeMillis();
        val dataCache1 = service.getBigDataCache();
        long cost = System.currentTimeMillis() - start;

        assertThat(cost).isLessThan(200L);
        assertThat(dataCache1).isEqualTo(bigDataXXX);

        Thread.sleep(250L - cost);

        val dataCache2 = service.getBigDataCache();
        assertThat(dataCache2).isEqualTo(bigDataYYY);

        snapshot.deleteSnapshotFile(cacheKey);
    }
}
