package com.github.bingoohuang.westcache;

import org.junit.Assert;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/16.
 */
public class WestCacheFactoryTest {
    @Test
    public void badCreate() {
        try {
            WestCacheFactory.create(XyzClass.class);
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Superclass has no null constructors but no arguments were given");
            return;
        }
        Assert.fail();
    }

    public static class XyzClass {
        public XyzClass(String sth) {
        }
    }
}
