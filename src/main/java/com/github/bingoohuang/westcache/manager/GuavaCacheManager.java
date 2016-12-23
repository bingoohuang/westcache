package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public class GuavaCacheManager extends BaseCacheManager {
    private Cache<String, Object> cache = CacheBuilder.newBuilder().<String, Object>build();
    private WestCache<String, Object> westCache = new WestCache<String, Object>() {
        @Override @SneakyThrows
        public Object get(String cacheKey, Callable<?> callable) {
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
    };


    @Override public WestCache<String, Object> getWestCache() {
        return westCache;
    }
}
