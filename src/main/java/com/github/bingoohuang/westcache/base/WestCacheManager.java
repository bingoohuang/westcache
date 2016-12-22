package com.github.bingoohuang.westcache.base;

import com.google.common.base.Optional;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCacheManager {
    <T> Optional<T> get(String cacheKey, Callable<Optional<T>> callable);

    <T> Optional<T> getSnapshot(String cacheKey, Callable<Optional<T>> callable);

    <T> Optional<T> get(String cacheKey);

    <T> void put(String cacheKey, Optional<T> cacheValue);
}
