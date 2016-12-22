package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.cachemanager.GuavaCacheManager;
import com.google.common.base.Optional;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class WestCache {
    static WestCacheManager westCacheManager = new GuavaCacheManager();

    public static <T extends Object> Optional<T> get(String cacheKey, Callable<Optional<T>> callable) {
        return westCacheManager.get(cacheKey, callable);
    }

    public static <T> Optional<T> get(String cacheKey) {
        return westCacheManager.get(cacheKey);
    }

    public static <T> Optional<T> getSnapshot(String cacheKey, Callable<Optional<T>> callable) {
        return westCacheManager.getSnapshot(cacheKey, callable);
    }

    public static <T> void put(String cacheKey, Optional<T> cacheValue) {
        westCacheManager.put(cacheKey, cacheValue);
    }
}
