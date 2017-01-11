package com.github.bingoohuang.westcache.springxml;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class ServiceSpringXmlTest {
    @Autowired XmlService service;

    @Test
    public void cachedMethod() {
        service.setData("Bingoo");
        assertThat(service.cachedMethod()).isEqualTo("Bingoo");

        service.setData("Pingoo");
        assertThat(service.cachedMethod()).isEqualTo("Bingoo");
    }
}
