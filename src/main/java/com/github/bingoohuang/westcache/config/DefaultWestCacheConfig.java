package com.github.bingoohuang.westcache.config;

import com.github.bingoohuang.westcache.base.WestCacheConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public class DefaultWestCacheConfig implements WestCacheConfig {
    @Override public long rotateCheckIntervalMillis() {
        return 15 * 60 * 1000; // 15 minutes
    }

    @Override public long timeoutMillisToSnapshot() {
        return 1000L;
    }

    @Override public ScheduledExecutorService executorService() {
        return Executors.newScheduledThreadPool(10);
    }
}
