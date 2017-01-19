package com.github.bingoohuang.westcache.utils;

import org.junit.Test;

import java.io.Closeable;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/11.
 */
public class EnvsTest {
    @Test
    public void classExists() {
        assertThat(Envs.classExists("a.b.C")).isFalse();
    }

    @Test(expected = InstantiationException.class)
    public void bad() {
        Envs.newInstance(Closeable.class.getName());
    }
}
