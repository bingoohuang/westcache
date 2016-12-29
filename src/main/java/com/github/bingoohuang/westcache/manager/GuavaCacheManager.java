package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.SneakyThrows;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public class GuavaCacheManager extends BaseCacheManager {
    public GuavaCacheManager() {
        super(new GuavaWestCache());
    }

    private static class GuavaWestCache implements WestCache {
        private Cache<String, WestCacheItem> cache = CacheBuilder.newBuilder().build();

        @Override @SneakyThrows
        public WestCacheItem get(WestCacheOption option, String cacheKey, Callable<WestCacheItem> callable) {
            try {
                return cache.get(cacheKey, callable);
            } catch (UncheckedExecutionException ex) {
                throw ex.getCause();
            }
        }

        @Override
        public WestCacheItem getIfPresent(WestCacheOption option, String cacheKey) {
            return cache.getIfPresent(cacheKey);
        }

        @Override
        public void put(WestCacheOption option, String cacheKey, WestCacheItem cacheValue) {
            cache.put(cacheKey, cacheValue);
        }

        @Override
        public void invalidate(WestCacheOption option, String cacheKey) {
            cache.invalidate(cacheKey);
        }
    }

}
