package com.github.bingoohuang.westcache.flusher;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheFlusher;
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
public class SimpleCacheFlusher implements WestCacheFlusher {
    @Getter private Cache<String, WestCache> registry;

    {
        registry = CacheBuilder.newBuilder().build();
    }

    @Override
    public boolean isKeyEnabled(WestCacheOption option, String cacheKey) {
        return true;
    }

    @Override
    public boolean register(WestCacheOption option,
                            String cacheKey,
                            WestCache cache) {
        val westCache = registry.getIfPresent(cacheKey);
        log.debug("register flush key {} for westcache {}", cacheKey, westCache);

        boolean registered = westCache == null;
        if (registered) registry.put(cacheKey, cache);
        return registered;
    }

    @Override public boolean flush(String cacheKey) {
        val westCache = registry.getIfPresent(cacheKey);
        log.debug("flush key:{}, westcache:{}", cacheKey, westCache);

        boolean flushSent = westCache != null;
        if (flushSent) westCache.invalidate(cacheKey);

        return flushSent;
    }

    @Override public <T> T getDirectValue(WestCacheOption option, String cacheKey) {
        return null;
    }

}
