package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.cachemanager.WestCacheManagerBuilder;
import com.google.common.base.Optional;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class WestCache {
    static WestCacheManager manager = WestCacheManagerBuilder.buildGuavaCacheManager();

    public static <T> Optional<T> get(
            String cacheKey, Callable<Optional<T>> callable) {
        return manager.get(cacheKey, callable);
    }

    public static <T> Optional<T> get(String cacheKey) {
        return manager.get(cacheKey);
    }

    public static <T> Optional<T> getSnapshot(
            String cacheKey, Callable<Optional<T>> callable) {
        return manager.getSnapshot(cacheKey, callable);
    }

    public static <T> void put(String cacheKey, Optional<T> cacheValue) {
        manager.put(cacheKey, cacheValue);
    }
}
