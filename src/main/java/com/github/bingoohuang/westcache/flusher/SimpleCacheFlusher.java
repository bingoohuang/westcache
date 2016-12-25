package com.github.bingoohuang.westcache.flusher;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheFlusher;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@Slf4j
public class SimpleCacheFlusher implements WestCacheFlusher {
    @Getter private Cache<String, WestCache<String, Object>> registry;

    {
        registry = CacheBuilder.newBuilder().build();
    }

    @Override
    public void register(String cacheKey, WestCache<String, Object> cache) {
        val westCache = registry.getIfPresent(cacheKey);
        log.debug("register flush key {} for westcache {}", cacheKey, westCache);
        if (westCache == null) registry.put(cacheKey, cache);
    }

    @Override public void flush(String cacheKey) {
        val westCache = registry.getIfPresent(cacheKey);
        log.debug("flush key:{}, westcache:{}", cacheKey, westCache);
        if (westCache != null) {
            westCache.invalidate(cacheKey);
            registry.invalidate(cacheKey);
        }
    }
}
