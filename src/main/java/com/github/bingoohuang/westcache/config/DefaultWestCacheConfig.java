package com.github.bingoohuang.westcache.config;

import com.github.bingoohuang.westcache.base.WestCacheConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public class DefaultWestCacheConfig implements WestCacheConfig {
    @Override public long timeoutMillisToSnapshot() {
        return 1000L;
    }

    @Override public ExecutorService executorService() {
        return Executors.newFixedThreadPool(10);
    }
}
