package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCacheManager {
    <T> Optional<T> get(WestCacheOption option, String cacheKey, Callable<Optional<T>> callable);

    <T> Optional<T> get(WestCacheOption option, String cacheKey);

    <T> void put(WestCacheOption option, String cacheKey, Optional<T> cacheValue);
}
