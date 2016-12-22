package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheManager;
import com.github.bingoohuang.westcache.base.WestCacheSnapshot;
import com.github.bingoohuang.westcache.manager.BaseCacheManager;
import com.github.bingoohuang.westcache.snapshot.FileCacheSnapshot;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import lombok.val;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class WestCacheManagerBuilder {
    private WestCache<String, Object> cache;
    private ExecutorService executorService = newFixedThreadPool(10);
    private WestCacheSnapshot westCacheSnapshot = new FileCacheSnapshot();

    public WestCacheManagerBuilder cache(WestCache<String, Object> cache) {
        this.cache = cache;
        return this;
    }

    public WestCacheManagerBuilder executorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    public WestCacheManagerBuilder westCacheSnapshot(WestCacheSnapshot westCacheSnapshot) {
        this.westCacheSnapshot = westCacheSnapshot;
        return this;
    }

    public WestCacheManager build() {
        return new BaseCacheManager(cache, executorService, westCacheSnapshot);
    }

    public static WestCacheManagerBuilder newBuilder() {
        return new WestCacheManagerBuilder();
    }

    public static WestCacheManager buildGuavaCacheManager() {
        final val cache = CacheBuilder.newBuilder().<String, Object>build();
        return newBuilder().cache(new WestCache<String, Object>() {
            @Override @SneakyThrows
            public Object get(String cacheKey, Callable<? extends Object> callable) {
                return cache.get(cacheKey, callable);
            }

            @Override public Object getIfPresent(String cacheKey) {
                return cache.getIfPresent(cacheKey);
            }

            @Override public void put(String cacheKey, Object cacheValue) {
                cache.put(cacheKey, cacheValue);
            }

            @Override public void invalidate(String cacheKey) {
                cache.invalidate(cacheKey);
            }
        }).build();
    }


}
