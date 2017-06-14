package com.github.bingoohuang.westcache.springann;

import com.alibaba.fastjson.JSON;
import com.github.bingoohuang.westcache.flusher.QuartzCacheFlusher;
import com.github.bingoohuang.westcache.manager.GuavaCacheManager;
import com.github.bingoohuang.westcache.spring.SpringAppContext;
import com.github.bingoohuang.westcache.utils.Envs;
import lombok.val;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.bingoohuang.westcache.WestCacheRegistry.FLUSHER_REGISTRY;
import static com.github.bingoohuang.westcache.WestCacheRegistry.MANAGER_REGISTRY;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfig.class})
public class DemoServiceTest {
    @Autowired DemoService service;

    @BeforeClass
    public static void beforeClass() {
        MANAGER_REGISTRY.register("quartz-manager", new GuavaCacheManager());
        FLUSHER_REGISTRY.register("quartz-flusher", new QuartzCacheFlusher());
    }

    @AfterClass
    public static void afterClass() {
        val flusher = (QuartzCacheFlusher) FLUSHER_REGISTRY.get("quartz-flusher");
        flusher.stopQuartz();

        MANAGER_REGISTRY.deregister("quartz-manager");
        FLUSHER_REGISTRY.deregister("quartz-flusher");
    }

    @Test
    public void cachedMethod() {
        service.setData("Bingoo");
        assertThat(service.cachedMethod()).isEqualTo("Bingoo");

        service.setData("Pingoo");
        assertThat(service.cachedMethod()).isEqualTo("Bingoo");

        String s1 = service.doSth();
        String s2 = service.doSth();
        assertThat(s1).isEqualTo(s2);

        ApplicationContext context = SpringAppContext.getAppContext();
        assertThat(context).isNotNull();

        Object xxxx = SpringAppContext.getBean("xxxx");
        assertThat(xxxx).isNull();

        Object bean = SpringAppContext.getBean(JSON.class);
        assertThat(bean).isNull();
    }

    @Test
    public void test1() {
        long l1 = service.doWhat();
        long l2 = service.doWhat();
        assertThat(l1).isEqualTo(l2);
        Envs.sleepMillis(1000);

        long l3 = service.doWhat();
        assertThat(l3).isGreaterThan(l1);
    }

    @Test
    public void ttlConfig() {
        long l1 = service.doTtl();
        long l2 = service.doTtl();
        assertThat(l1).isEqualTo(l2);

        Envs.sleepMillis(1100);
        long l3 = service.doTtl();
        assertThat(l3).isGreaterThan(l1);
    }

    @Test
    public void doEx() {
        try {
            service.doEx();
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("i am do exception");
            return;
        }
        Assert.fail();
    }
}
