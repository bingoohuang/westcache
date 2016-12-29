package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCache{
    Optional<Object> get(WestCacheOption option, String cacheKey, Callable<Optional<Object>> callable);

    Optional<Object> getIfPresent(WestCacheOption option, String cacheKey);

    void put(WestCacheOption option, String cacheKey, Optional<Object> cacheValue);

    void invalidate(WestCacheOption option, String cacheKey);
}
