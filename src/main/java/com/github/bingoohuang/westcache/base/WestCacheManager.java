package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.utils.WestCacheOption;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCacheManager {
    WestCacheItem get(WestCacheOption option, String cacheKey, Callable<WestCacheItem> callable);

    WestCacheItem get(WestCacheOption option, String cacheKey);

    void put(WestCacheOption option, String cacheKey, WestCacheItem cacheValue);

    void invalidate(WestCacheOption option, String cacheKey, String version);
}
