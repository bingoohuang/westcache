package com.github.bingoohuang.westcache.base;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCache<K, V> {
    V get(K cacheKey, Callable<? extends V> callable);

    V getIfPresent(K cacheKey);

    void put(K cacheKey, V cacheValue);
}
