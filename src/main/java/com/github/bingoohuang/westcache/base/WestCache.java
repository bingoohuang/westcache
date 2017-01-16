package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.utils.WestCacheOption;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCache {
    /**
     * Get the cached item from cache with firstly built.
     * <p>
     * The cache implementation should use callable to create cache value
     * when the cache is firstly built.
     *
     * @param option   westcache settings.
     * @param cacheKey cache key.
     * @param callable under callable when cache is first built.
     * @return cache item.
     */
    WestCacheItem get(WestCacheOption option,
                      String cacheKey,
                      Callable<WestCacheItem> callable);

    /**
     * Try to get the cached item from the cache if present.
     *
     * @param option   westcache settings.
     * @param cacheKey cache key.
     * @return cache item or null if not present.
     */
    WestCacheItem getIfPresent(WestCacheOption option,
                               String cacheKey);

    /**
     * Put the cache value to the cache.
     *
     * @param option     westcache settings.
     * @param cacheKey   cache key.
     * @param cacheValue cache value.
     */
    void put(WestCacheOption option,
             String cacheKey,
             WestCacheItem cacheValue);

    /**
     * Invalidate the cache。
     *
     * @param option   westcache settings.
     * @param cacheKey cache key.
     * @param version  version string.
     */
    void invalidate(WestCacheOption option, String cacheKey, String version);
}
