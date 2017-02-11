package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.base.WestCacheKeyer;
import com.github.bingoohuang.westcache.manager.BaseCacheManager;
import com.github.bingoohuang.westcache.manager.DiamondCacheManager;
import com.github.bingoohuang.westcache.utils.WestCacheConnector;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.SneakyThrows;
import org.junit.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
public class DiamondManagerTest {
    public interface DiamondService {
        @WestCacheable(manager = "diamond", specs = "static.key=yes")
        String getBigData();
    }

    static DiamondService service = WestCacheFactory.create(DiamondService.class);
    static WestCacheOption option = WestCacheConnector.connectOption(new Runnable() {
        @Override public void run() {
            service.getBigData();
        }
    });
    static WestCacheKeyer keyer = option.getKeyer();
    static String cacheKey = keyer.getCacheKey(option, "getBigData", service);
    static BaseCacheManager manager = (BaseCacheManager) option.getManager();

    @Test @SneakyThrows
    public void test() {
        String content = "Here is Bingoo!" + System.currentTimeMillis();
        MockDiamondServer.setConfigInfo(DiamondCacheManager.GROUP, cacheKey, content);

        assertThat(service.getBigData()).isEqualTo(content);

        WestCacheItem item = manager.getWestCache().getIfPresent(option, cacheKey);
        assertThat(item.orNull()).isEqualTo(content);
    }

    @Test(expected = UnsupportedOperationException.class) @SneakyThrows
    public void put() {
        manager.put(null, cacheKey, null);
    }

    @Test(expected = UnsupportedOperationException.class) @SneakyThrows
    public void invalidate() {
        manager.getWestCache().invalidate(null, cacheKey, null);
    }
}
