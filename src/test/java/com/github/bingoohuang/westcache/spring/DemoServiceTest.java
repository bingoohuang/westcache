package com.github.bingoohuang.westcache.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfig.class})
public class DemoServiceTest {
    @Autowired DemoService service;

    @Test
    public void cachedMethod() {
        service.setData("Bingoo");
        assertThat(service.cachedMethod()).isEqualTo("Bingoo");

        service.setData("Pingoo");
        assertThat(service.cachedMethod()).isEqualTo("Bingoo");

        String s1 = service.doSth();
        String s2 = service.doSth();
        assertThat(s1).isEqualTo(s2);
    }
}
