package com.github.bingoohuang.westcache.spring;

import com.github.bingoohuang.westcache.WestCacheRegistry;
import com.github.bingoohuang.westcache.manager.DiamondCacheManager;
import com.github.bingoohuang.westcache.utils.Diamonds;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.bingoohuang.westcache.utils.WestCacheOption.newBuilder;
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
        val keyer = WestCacheRegistry.getKeyer("default");
        val option = newBuilder().manager("diamond").specs("static.key=yes").build();
        val cacheKey = keyer.getCacheKey(option, "getBigData", service);
        String content = "Here is Bingoo!" + System.currentTimeMillis();
        Diamonds.writeDiamondJSON(DiamondCacheManager.GROUP, cacheKey, content);

        assertThat(service.getBigData()).isEqualTo(content);
    }

}
