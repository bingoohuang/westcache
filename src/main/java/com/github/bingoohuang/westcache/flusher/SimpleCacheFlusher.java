package com.github.bingoohuang.westcache.flusher;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@Slf4j
public class SimpleCacheFlusher extends ByPassCacheFlusher {
    @Getter private Cache<String, WestCache>
            registry = CacheBuilder.newBuilder().build();

    @Override
    public boolean register(WestCacheOption option,
                            String cacheKey,
                            WestCache cache) {
        val westCache = registry.getIfPresent(cacheKey);
        log.debug("register flush key {} for cache {}", cacheKey, westCache);

        val registered = westCache == null;
        if (registered) registry.put(cacheKey, cache);
        return registered;
    }

    @Override public boolean flush(WestCacheOption option,
                                   String cacheKey,
                                   String version) {
        val westCache = registry.getIfPresent(cacheKey);
        log.debug("flush key {} for cache {}", cacheKey, westCache);

        val flushSent = westCache != null;
        if (flushSent) westCache.invalidate(option, cacheKey, version);

        return flushSent;
    }
}
