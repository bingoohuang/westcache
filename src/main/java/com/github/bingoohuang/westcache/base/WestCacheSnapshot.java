package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCacheSnapshot {
    /**
     * save the cached value to snapshot place.
     *
     * @param option
     * @param cacheKey   cache key.
     * @param cacheValue cache value.
     */
    void saveSnapshot(WestCacheOption option, String cacheKey, Object cacheValue);

    /**
     * read the cached value from snapshot place.
     *
     * @param option
     * @param cacheKey cache key.  @return optional of cache value
     */
    Optional<Object> readSnapshot(WestCacheOption option, String cacheKey);

    /**
     * clear the cached value from snapshot place.
     *
     * @param cacheKey cache key.
     */
    void deleteSnapshot(String cacheKey);
}
