package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.manager.DiamondCacheManager;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import static com.github.bingoohuang.westcache.WestCacheRegistry.keyerRegistry;
import static com.github.bingoohuang.westcache.utils.WestCacheOption.newBuilder;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
public class DiamondManagerTest {
    public interface DiamondService {
        @WestCacheable(manager = "diamond", specs = "static.key=yes")
        String getBigData();
    }

    @Test @SneakyThrows
    public void test() {
        val service = WestCacheFactory.create(DiamondService.class);
        val keyer = keyerRegistry.get("default");
        val option = newBuilder().manager("diamond").specs("static.key=yes").build();
        val cacheKey = keyer.getCacheKey(option, "getBigData", service);
        String content = "Here is Bingoo!" + System.currentTimeMillis();

        MockDiamondServer.setConfigInfo(DiamondCacheManager.GROUP, cacheKey, content);

        assertThat(service.getBigData()).isEqualTo(content);
    }
}
