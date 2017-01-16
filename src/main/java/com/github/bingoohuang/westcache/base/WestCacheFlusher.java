package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCacheFlusher {
    /**
     * Tell whether the key is enabled for cache.
     *
     * @param option   WestCacheOption.
     * @param cacheKey Cache key.
     * @return enabled or not.
     */
    boolean isKeyEnabled(WestCacheOption option, String cacheKey);

    /**
     * Try to get direct value from another place like db table value.
     *
     * @param option   WestCacheOption.
     * @param cacheKey Cache key.
     * @return Direct value.
     */
    Optional<Object> getDirectValue(WestCacheOption option, String cacheKey);

    /**
     * Register cache key for flusher.
     *
     * @param option   WestCacheOption.
     * @param cacheKey Cache key.
     * @param cache    Cache
     * @return If Registered at first time.
     */
    boolean register(WestCacheOption option,
                     String cacheKey,
                     WestCache cache);

    /**
     * Flush cache.
     *
     * @param option   WestCacheOption.
     * @param cacheKey Cache key.
     * @param version  Version String.
     * @return flushed or not.
     */
    boolean flush(WestCacheOption option, String cacheKey, String version);
}
