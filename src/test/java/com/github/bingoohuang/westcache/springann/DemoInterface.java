package com.github.bingoohuang.westcache.springann;

import com.github.bingoohuang.westcache.WestCacheable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
public interface DemoInterface {
    @WestCacheable(manager = "diamond", specs = "static.key=yes")
    String getBigData();
}
