package com.github.bingoohuang.westcache.utils;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/11.
 */
public class DurationsTest {
    @Test
    public void test() {
        long expire1 = Durations.parse("expire", "1s", TimeUnit.SECONDS);
        assertThat(expire1).isEqualTo(1L);

        long expire2 = Durations.parse("expire", "1m", TimeUnit.MINUTES);
        assertThat(expire2).isEqualTo(1L);

        long expire3 = Durations.parse("expire", "1h", TimeUnit.HOURS);
        assertThat(expire3).isEqualTo(1L);

        long expire4 = Durations.parse("expire", "1d", TimeUnit.DAYS);
        assertThat(expire4).isEqualTo(1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exception1() {
        Durations.parse("expire", "1x", TimeUnit.SECONDS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exception2() {
       Durations.parse("expire", "xx", TimeUnit.SECONDS);
    }
}
