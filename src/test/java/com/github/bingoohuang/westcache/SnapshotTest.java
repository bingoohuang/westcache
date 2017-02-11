package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.utils.Envs;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.github.bingoohuang.westcache.WestCacheRegistry.REGISTRY_TEMPLATE;
import static com.github.bingoohuang.westcache.WestCacheRegistry.SNAPSHOT_REGISTRY;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class SnapshotTest {
    public static abstract class BasicSnapshotService {
        @Getter @Setter String bigData;
        @Setter long sleepMillis = 150L;
        @Getter @Setter private volatile boolean cacheMethodExecuted = false;

        abstract String getBigDataCache();

        @SneakyThrows
        public String getBigDataSlow() {
            // some milliseconds to simulate slow of reading big data
            Envs.sleepMillis(sleepMillis);
            setCacheMethodExecuted(true);
            return bigData;
        }
    }

    @BeforeClass
    public static void beforeClass() {
        REGISTRY_TEMPLATE.register("snapshotTestConfig", new DefaultWestCacheConfig() {
            @Override
            public long timeoutMillisToSnapshot() {
                return 100L;
            }
        });
    }

    @AfterClass
    public static void afterClass() {
        REGISTRY_TEMPLATE.deregister("snapshotTestConfig");
    }

    public static class SnapshotService extends BasicSnapshotService {
        @WestCacheable(snapshot = "file", config = "snapshotTestConfig")
        public String getBigDataCache() {
            return getBigDataSlow();
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @WestCacheable(snapshot = "file", config = "snapshotTestConfig")
    public @interface WestCacheableSnapshot {
    }

    public static class SnapshotServiceCustomizedAnnotation extends BasicSnapshotService {

        @WestCacheableSnapshot
        @SneakyThrows
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

    public void snapshot(Class<? extends BasicSnapshotService> serviceClass) {
        val bigDataXXX = "SnapshotService.getBigData.XXX";
        val bigDataYYY = "SnapshotService.getBigData.YYY";

        val snapshot = SNAPSHOT_REGISTRY.get("file");
        val cacheKey = serviceClass.getName().replace('$', '.') + ".getBigDataCache";
        Optional<Object> optional = Optional.<Object>fromNullable(bigDataXXX);
        snapshot.saveSnapshot(null, cacheKey, new WestCacheItem(optional, null));

        val service = WestCacheFactory.create(serviceClass);
        service.setBigData(bigDataYYY);

        service.setCacheMethodExecuted(false);
        val dataCache1 = service.getBigDataCache();
        assertThat(dataCache1).isEqualTo(bigDataXXX);

        do {
            Envs.sleepMillis(50L);
        } while (!service.isCacheMethodExecuted());

        val dataCache2 = service.getBigDataCache();
        assertThat(dataCache2).isEqualTo(bigDataYYY);

        snapshot.deleteSnapshot(null, cacheKey);
    }
}
