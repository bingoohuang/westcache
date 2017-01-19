package com.github.bingoohuang.westcache.utils;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/11.
 */
public class DurationsTest {
    @Test
    public void test() {
        long expire1 = Durations.parse("expire", "1s");
        assertThat(expire1).isEqualTo(1L);

        long expire2 = Durations.parse("expire", "1m");
        assertThat(expire2).isEqualTo(60L);

        long expire3 = Durations.parse("expire", "1h");
        assertThat(expire3).isEqualTo(60L * 60L);

        long expire4 = Durations.parse("expire", "1d");
        assertThat(expire4).isEqualTo(24L * 60L * 60L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exception1() {
        Durations.parse("expire", "1x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void exception2() {
        Durations.parse("expire", "xs");
    }
}
