package com.github.bingoohuang.westcache.base;

import com.google.common.base.Optional;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCache{
    Optional<Object> get(String cacheKey, Callable<Optional<Object>> callable);

    Optional<Object> getIfPresent(String cacheKey);

    void put(String cacheKey, Optional<Object> cacheValue);

    void invalidate(String cacheKey);
}
