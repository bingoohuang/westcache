package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.utils.lang.Clz;
import com.github.bingoohuang.utils.lang.Futures;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Test;

import java.io.Closeable;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/11.
 */
public class EnvsTest {
    @Test
    public void classExists() {
        assertThat(Clz.classExists("a.b.C")).isFalse();
    }

    @Test(expected = ClassNotFoundException.class)
    public void classNotFound() {
        Clz.forName("a.b.C");
    }


    @Test(expected = InstantiationException.class)
    public void bad() {
        Clz.newInstance(Closeable.class.getName());
    }

    @Test(expected = RuntimeException.class)
    public void futureGetException() {
        SettableFuture<Object> future = SettableFuture.create();
        future.setException(new RuntimeException());
        Futures.futureGet(future);

        SettableFuture<Object> future2 = SettableFuture.create();
        future2.set(new Object());
        Futures.futureGet(future2);

    }

    @Test
    public void futureGet() {
        SettableFuture<Object> future = SettableFuture.create();
        future.set(new Object());
        Futures.futureGet(future);
    }
}
