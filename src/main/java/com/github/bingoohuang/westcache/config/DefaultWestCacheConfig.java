package com.github.bingoohuang.westcache.config;

import com.github.bingoohuang.westcache.base.WestCacheConfig;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public class DefaultWestCacheConfig implements WestCacheConfig {
    @Override public long rotateIntervalMillis() {
        return 60 * 1000L;
    }

    @Override public long timeoutMillisToSnapshot() {
        return 1000L;
    }
}
