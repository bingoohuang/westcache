package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheFlusher;
import com.github.bingoohuang.westcache.impl.WestCacheOption;
import com.github.bingoohuang.westcache.utils.CacheKeyUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@UtilityClass
public class WestCacheFlusherManager {
    Cache<String, WestCacheFlusher> registry = CacheBuilder.newBuilder().build();

    public void registerFlusher(String flusherName, WestCacheFlusher flusher) {
        WestCacheFlusher cacheFlusher = registry.getIfPresent(flusherName);
        if (cacheFlusher != null) throw new RuntimeException(
                "flush name " + flusherName + " already exists");

        registry.put(flusherName, flusher);
    }

    public WestCacheFlusher get(String flusherName) {
        return registry.getIfPresent(flusherName);
    }

    public void registerCacheKey(WestCache<String, Object> cache,
                                 WestCacheOption option,
                                 String cacheKey) {
        val flusher = option.getWestCacheFlusher();
        if (flusher == null) return;

        flusher.register(cacheKey, cache);
    }

    public void flush(WestCacheOption option, String cacheKey) {
        val flusher = option.getWestCacheFlusher();
        if (flusher == null) return;

        flusher.flush(cacheKey);
    }

    public void flush(WestCacheOption option, Object bean, String methodName) {
        String cacheKey = option.isSnapshot()
                ? CacheKeyUtils.createSnapshotCacheKey(bean, methodName)
                : CacheKeyUtils.createCacheKey(bean, methodName);

        flush(option, cacheKey);
    }
}
