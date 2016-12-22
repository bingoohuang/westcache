package com.github.bingoohuang.westcache;

import com.alibaba.fastjson.JSON;
import com.github.bingoohuang.westcache.impl.FileSnapshotUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;

import static com.alibaba.fastjson.serializer.SerializerFeature.WriteClassName;
import static com.github.bingoohuang.westcache.impl.CacheKeyUtils.createCacheKey;
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
        val file = FileSnapshotUtils.getSnapshotFile(cacheKey);
        val json = JSON.toJSONString(bigDataXXX, WriteClassName);
        Files.write(json, file, Charsets.UTF_8);

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
