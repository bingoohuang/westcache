package com.github.bingoohuang.westcache.flusher;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheFlusher;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@Slf4j
public class SimpleCacheFlusher implements WestCacheFlusher {
    Cache<String, WestCache<String, Object>> registry = CacheBuilder.newBuilder().build();

    @Override
    public void register(String cacheKey, WestCache<String, Object> cache) {
        log.debug("register {}", cacheKey);
        if (registry.getIfPresent(cacheKey) != null) return;

        registry.put(cacheKey, cache);
    }

    @Override public void flush(String cacheKey) {
        log.debug("flush {}", cacheKey);
        val westCache = registry.getIfPresent(cacheKey);
        if (westCache == null) return;

        westCache.invalidate(cacheKey);
    }
}
