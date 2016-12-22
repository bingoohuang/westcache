package com.github.bingoohuang.westcache.cachemanager;

import com.github.bingoohuang.westcache.WestCacheSnapshot;
import com.github.bingoohuang.westcache.snapshot.FileCacheSnapshot;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class GuavaCacheManager extends AbstractCacheManager {
    Cache<String, Object> cache = CacheBuilder.newBuilder().build();
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    WestCacheSnapshot cacheSnapshot = new FileCacheSnapshot();


    @Override protected Cache<String, Object> getCache() {
        return cache;
    }

    @Override protected ExecutorService getExecutorService() {
        return executorService;
    }

    @Override protected WestCacheSnapshot getCacheSnapshot() {
        return cacheSnapshot;
    }
}
