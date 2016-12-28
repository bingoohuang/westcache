package com.github.bingoohuang.westcache.base;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public interface WestCacheConfig {
    long timeoutMillisToSnapshot();

    ScheduledExecutorService executorService();
}
