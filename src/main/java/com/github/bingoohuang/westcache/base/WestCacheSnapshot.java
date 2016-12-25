package com.github.bingoohuang.westcache.base;

import com.google.common.base.Optional;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCacheSnapshot {
    /**
     * save the cached value to snapshot place.
     * @param cacheKey cache key.
     * @param cacheValue cache value.
     */
    void saveSnapshot(String cacheKey, Object cacheValue);

    /**
     * read the cached value from snapshot place.
     * @param cacheKey cache key.
     * @param <T> cache value type
     * @return optional of cache value
     */
    <T> Optional<T> readSnapshot(String cacheKey);

    /**
     * clear the cached value from snapshot place.
     * @param cacheKey cache key.
     */
    void deleteSnapshot(String cacheKey);
}
