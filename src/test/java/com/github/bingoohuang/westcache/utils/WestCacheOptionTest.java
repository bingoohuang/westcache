package com.github.bingoohuang.westcache.utils;

import org.junit.Test;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/11.
 */
public class WestCacheOptionTest {
    @Test(expected = RuntimeException.class)
    public void test() {
        WestCacheOption.newBuilder().manager("fuck");
    }
}
