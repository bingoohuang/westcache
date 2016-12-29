package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.utils.WestCacheOption;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCacheSnapshot {
    /**
     * save the cached value to snapshot place.
     *
     * @param option     WestCache settings.
     * @param cacheKey   cache key.
     * @param cacheValue cache value.
     */
    void saveSnapshot(WestCacheOption option, String cacheKey, WestCacheItem cacheValue);

    /**
     * read the cached value from snapshot place.
     *
     * @param option   WestCache settings.
     * @param cacheKey cache key.
     * @return optional of cache value
     */
    WestCacheItem readSnapshot(WestCacheOption option, String cacheKey);

    /**
     * clear the cached value from snapshot place.
     *
     * @param option   WestCache settings.
     * @param cacheKey cache key.
     */
    void deleteSnapshot(WestCacheOption option, String cacheKey);
}
