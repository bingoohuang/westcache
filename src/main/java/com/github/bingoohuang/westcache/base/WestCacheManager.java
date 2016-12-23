package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.WestCacheOptions;
import com.google.common.base.Optional;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCacheManager {
    <T> Optional<T> get(WestCacheOptions option, String cacheKey, Callable<Optional<T>> callable);

    <T> Optional<T> getSnapshot(WestCacheOptions option, String cacheKey, Callable<Optional<T>> callable);

    <T> Optional<T> get(WestCacheOptions option, String cacheKey);

    <T> void put(WestCacheOptions option, String cacheKey, Optional<T> cacheValue);
}
