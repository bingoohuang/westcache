package com.github.bingoohuang.westcache;

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
    @Test @SneakyThrows
    public void snapshot() {
        val bigDataXXX = "SnapshotService.getBigData.XXX";
        val bigDataYYY = "SnapshotService.getBigData.YYY";

        val methodName = "getBigDataCache";
        val cacheKey = createCacheKey(SnapshotService.class, methodName);

        val snapshot = new FileCacheSnapshot();
        snapshot.saveSnapshot(cacheKey, bigDataXXX);

        val service = WestCacheFactory.create(SnapshotService.class);
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

    public static class SnapshotService {
        @Getter @Setter private String bigData;

        @SneakyThrows
        @WestCacheable(snapshot = true)
        public String getBigDataCache() {
            // 3 seconds to simulate slow of reading big data
            Thread.sleep(700L);
            return bigData;
        }
    }
}
