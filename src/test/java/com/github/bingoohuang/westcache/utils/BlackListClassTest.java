package com.github.bingoohuang.westcache.utils;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class BlackListClassTest {
    @Test
    public void test() {
        assertThat(BlackListClass.inBlackList("com.github.bingoohuang.westcache.springann.SpringAnnDaoTest")).isTrue();
        assertThat(BlackListClass.inBlackList("com.github.SpringAnnDaoTest")).isFalse();
    }
}
