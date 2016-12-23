package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheFlusher;
import com.github.bingoohuang.westcache.flusher.NoneCacheFlusher;
import com.github.bingoohuang.westcache.flusher.SimpleCacheFlusher;
import com.github.bingoohuang.westcache.impl.WestCacheOption;
import com.github.bingoohuang.westcache.utils.CacheKeyUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.experimental.UtilityClass;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@UtilityClass
public class WestCacheFlusherRegistry {
    Cache<String, WestCacheFlusher> registry = CacheBuilder.newBuilder().build();

    static {
        registerFlusher("none", new NoneCacheFlusher());
        registerFlusher("simple", new SimpleCacheFlusher());
    }

    public void registerFlusher(String flusherName, WestCacheFlusher flusher) {
        WestCacheFlusher cacheFlusher = registry.getIfPresent(flusherName);
        if (cacheFlusher != null) throw new RuntimeException(
                "flush name " + flusherName + " already exists");

        registry.put(flusherName, flusher);
    }

    public void deregisterFlusher(String flusherName) {
        registry.invalidate(flusherName);
    }

    public WestCacheFlusher get(String flusherName) {
        return registry.getIfPresent(flusherName);
    }

    public void flush(WestCacheOption option, Object bean, String methodName) {
        String cacheKey = option.getSnapshot() != null
                ? CacheKeyUtils.createSnapshotCacheKey(bean, methodName)
                : CacheKeyUtils.createCacheKey(bean, methodName);

        option.getFlusher().flush(cacheKey);
    }
}
