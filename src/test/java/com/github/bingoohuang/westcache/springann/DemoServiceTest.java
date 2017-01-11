package com.github.bingoohuang.westcache.springann;

import com.alibaba.fastjson.JSON;
import com.github.bingoohuang.westcache.spring.SpringAppContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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

        ApplicationContext context = SpringAppContext.getAppContext();
        assertThat(context).isNotNull();

        Object xxxx = SpringAppContext.getBean("xxxx");
        assertThat(xxxx).isNull();

        Object bean = SpringAppContext.getBean(JSON.class);
        assertThat(bean).isNull();
    }
}
