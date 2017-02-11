package com.github.bingoohuang.westcache.base;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public interface WestCacheConfig {
    /**
     * The rotate check interval milliseconds.
     *
     * @return interval millis.
     */
    long rotateIntervalMillis();

    /**
     * The raw invoke timeout millis for snapshot.
     *
     * @return timeout millis.
     */
    long timeoutMillisToSnapshot();
}
