package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.google.common.base.Optional;
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
        private Cache<String, Optional<Object>> cache = CacheBuilder.newBuilder().build();

        @Override @SneakyThrows
        public Optional<Object> get(String cacheKey, Callable<Optional<Object>> callable) {
            try {
                return cache.get(cacheKey, callable);
            } catch (UncheckedExecutionException ex) {
                throw ex.getCause();
            }
        }

        @Override public Optional<Object> getIfPresent(String cacheKey) {
            return cache.getIfPresent(cacheKey);
        }

        @Override
        public void put(String cacheKey, Optional<Object> cacheValue) {
            cache.put(cacheKey, cacheValue);
        }

        @Override public void invalidate(String cacheKey) {
            cache.invalidate(cacheKey);
        }
    }

}
