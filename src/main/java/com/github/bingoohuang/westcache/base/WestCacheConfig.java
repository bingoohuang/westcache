package com.github.bingoohuang.westcache.base;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public interface WestCacheConfig {
    /**
     * The rotate check interval milliseconds.
     *
     * @return interval millis.
     */
    long rotateCheckIntervalMillis();

    /**
     * The raw invoke timeout millis for snapshot.
     *
     * @return timeout millis.
     */
    long timeoutMillisToSnapshot();

    /**
     * The thread pool for snapshot timeout and rotate checker.
     *
     * @return thread pool.
     */
    ScheduledExecutorService executorService();
}
