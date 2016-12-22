package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheable;
import com.github.bingoohuang.westcache.snapshot.FileCacheSnapshot;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;

import static com.github.bingoohuang.westcache.utils.CacheKeyUtils.createCacheKey;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class SnapshotTest {
    public static abstract class BasicSnapshotService {
        @Getter @Setter String bigData;

        abstract String getBigDataCache();

        @SneakyThrows
        public String getBigDataSlow() {
            // 700 milliseconds to simulate slow of reading big data
            Thread.sleep(700L);
            return bigData;
        }
    }

    public static class SnapshotService extends BasicSnapshotService {
        @WestCacheable(snapshot = true)
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

        val methodName = "getBigDataCache";
        val cacheKey = createCacheKey(serviceClass, methodName);

        val snapshot = new FileCacheSnapshot();
        snapshot.saveSnapshot(cacheKey, bigDataXXX);

        BasicSnapshotService service = WestCacheFactory.create(serviceClass);
        service.setBigData(bigDataYYY);

        long start = System.currentTimeMillis();
        val dataCache1 = service.getBigDataCache();
        long cost = System.currentTimeMillis() - start;

        assertThat(cost).isLessThan(700L);
        assertThat(dataCache1).isEqualTo(bigDataXXX);

        Thread.sleep(700L - cost + 100);

        val dataCache2 = service.getBigDataCache();
        assertThat(dataCache2).isEqualTo(bigDataYYY);

        snapshot.deleteSnapshotFile(cacheKey);
    }
}
