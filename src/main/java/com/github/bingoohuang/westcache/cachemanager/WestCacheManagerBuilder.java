package com.github.bingoohuang.westcache.cachemanager;

import com.github.bingoohuang.westcache.WestCacheManager;
import com.github.bingoohuang.westcache.WestCacheSnapshot;
import com.github.bingoohuang.westcache.snapshot.FileCacheSnapshot;
import com.google.common.cache.Cache;
import lombok.val;

import java.util.concurrent.ExecutorService;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class WestCacheManagerBuilder {
    private Cache<String, Object> cache;
    private ExecutorService executorService = newFixedThreadPool(10);
    private WestCacheSnapshot westCacheSnapshot = new FileCacheSnapshot();

    public WestCacheManagerBuilder cache(Cache<String, Object> cache) {
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

    public static WestCacheManager buildGuavaCacheManager() {
        val cache = newBuilder().<String, Object>build();
        return new WestCacheManagerBuilder().cache(cache).build();
    }
}
