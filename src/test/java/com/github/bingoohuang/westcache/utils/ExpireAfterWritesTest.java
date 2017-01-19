package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.base.ExpireAfterWrite;
import org.junit.Assert;
import org.junit.Test;

import static com.github.bingoohuang.westcache.utils.ExpireAfterWrites.findExpireAfterWriteMethod;
import static com.github.bingoohuang.westcache.utils.ExpireAfterWrites.getExpireAfterWrite;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/19.
 */
public class ExpireAfterWritesTest {
    public static class ServiceEawBean {
        @ExpireAfterWrite
        public int expireAfterWrite() {
            return 0;
        }
    }

    public static class ServiceEawBean2 {
        @ExpireAfterWrite
        public String expireAfterWrite() {
            throw new RuntimeException("I mean it");
        }
    }

    @Test
    public void badSigature() {
        try {
            findExpireAfterWriteMethod(new ServiceEawBean());
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo(
                    "method ServiceEawBean.expireAfterWrite " +
                            "is invalid for @ExpireAfterWrite");
            return;
        }

        Assert.fail();
    }


    @Test
    public void badInvoke() {
        try {
            getExpireAfterWrite(new ServiceEawBean2());
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo(
                    "I mean it");
            return;
        }

        Assert.fail();
    }
}
