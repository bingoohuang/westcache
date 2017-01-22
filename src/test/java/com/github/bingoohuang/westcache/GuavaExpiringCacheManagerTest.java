package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.manager.BaseCacheManager;
import com.github.bingoohuang.westcache.utils.Envs;
import com.github.bingoohuang.westcache.utils.WestCacheConnector;
import com.google.common.base.Optional;
import lombok.Setter;
import lombok.val;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/22.
 */
public class GuavaExpiringCacheManagerTest {
    public static class GuavaDemoService {
        @Setter private long timestamp;
        @Setter private long timestamp3;

        @WestCacheable(specs = "expireAfterWrite=1s")
        public String cacheWrite() {
            return "Write@" + timestamp;
        }

        @WestCacheable
        public String cacheThree() {
            return "Three@" + timestamp3;
        }
    }

    static GuavaDemoService service = WestCacheFactory.create(GuavaDemoService.class);

    @Test
    public void put() {
        Runnable runnable = new Runnable() {
            @Override public void run() {
                service.cacheThree();
            }
        };

        val option = WestCacheConnector.connectOption(runnable);
        val manager = (BaseCacheManager) option.getManager();
        val cacheKey = WestCacheConnector.connectKey(runnable);

        manager.getWestCache().put(null, cacheKey,
                new WestCacheItem(Optional.of("fuck"), option));
        val cacheValue3 = service.cacheThree();
        assertThat(cacheValue3).isEqualTo("fuck");

        manager.getWestCache().invalidate(option, cacheKey, "");
        service.setTimestamp3(10L);
        val cacheValue31 = service.cacheThree();
        assertThat(cacheValue31).isEqualTo("Three@10");
    }

    @Test
    public void expiringAfterWrite() {
        service.setTimestamp(10L);
        val cacheValue1 = service.cacheWrite();
        assertThat(cacheValue1).isEqualTo("Write@10");
        Envs.sleepMillis(500L);
        service.setTimestamp(3L);
        val cacheValue2 = service.cacheWrite();
        assertThat(cacheValue1).isSameAs(cacheValue2);

        Envs.sleepMillis(600L);
        Runnable runnable = new Runnable() {
            @Override public void run() {
                service.cacheWrite();
            }
        };
        val option = WestCacheConnector.connectOption(runnable);
        val manager = (BaseCacheManager) option.getManager();
        val cacheKey = WestCacheConnector.connectKey(runnable);
        val item = manager.getWestCache().getIfPresent(option, cacheKey);
        assertThat(item).isNull();

        val cacheValue3 = service.cacheWrite();
        assertThat(cacheValue3).isEqualTo("Write@3");
        val cacheValue4 = service.cacheWrite();
        assertThat(cacheValue3).isSameAs(cacheValue4);

        Envs.sleepMillis(1100L);
        service.setTimestamp(5L);
        val cacheValue5 = service.cacheWrite();
        assertThat(cacheValue5).isEqualTo("Write@5");
    }
}
