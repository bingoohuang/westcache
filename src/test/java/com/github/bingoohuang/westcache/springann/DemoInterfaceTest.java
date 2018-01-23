package com.github.bingoohuang.westcache.springann;

import com.github.bingoohuang.westcache.manager.DiamondCacheManager;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.bingoohuang.westcache.WestCacheRegistry.KEYER_REGISTRY;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfig.class})
public class DemoInterfaceTest {
    @Autowired DemoInterface service;

    @Test @SneakyThrows
    public void test() {
        val keyer = KEYER_REGISTRY.get("default");
        val option = WestCacheOption.builder().manager("diamond").specs("static.key=yes").build();
        val cacheKey = keyer.getCacheKey(option, "getBigData", service);
        String content = "Here is Bingoo!" + System.currentTimeMillis();
        MockDiamondServer.setConfigInfo(DiamondCacheManager.GROUP, cacheKey, content);

        assertThat(service.getBigData()).isEqualTo(content);
    }

}
