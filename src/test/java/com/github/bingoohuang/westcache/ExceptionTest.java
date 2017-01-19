package com.github.bingoohuang.westcache;

import org.junit.Assert;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public class ExceptionTest {
    static class MyTxException extends RuntimeException {
        public MyTxException(String msg) {
            super(msg);
        }
    }

    static class ExService {
        @WestCacheable
        public String iWillThrowException() {
            throw new MyTxException("bingoo do this");
        }

        @WestCacheable(snapshot = "file")
        public String iWillThrowExceptionSnapshot() {
            throw new MyTxException("bingoo do this");
        }
    }

    static ExService sevice = WestCacheFactory.create(ExService.class);

    @Test
    public void iWillThrowException() {
        try {
            sevice.iWillThrowException();
        } catch (MyTxException ex) {
            assertThat(ex.getMessage()).isEqualTo("bingoo do this");
            return;
        }

        Assert.fail();
    }

    @Test
    public void iWillThrowExceptionSnapshot() {
        try {
            sevice.iWillThrowExceptionSnapshot();
        } catch (MyTxException ex) {
            assertThat(ex.getMessage()).isEqualTo("bingoo do this");
            return;
        }

        Assert.fail();
    }
}
