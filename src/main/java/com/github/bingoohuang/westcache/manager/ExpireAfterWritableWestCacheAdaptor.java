package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/22.
 */
@AllArgsConstructor
public class ExpireAfterWritableWestCacheAdaptor implements WestCache {
    private final WestCache westCache;

    @Override
    public WestCacheItem get(WestCacheOption option,
                             String cacheKey,
                             Callable<WestCacheItem> callable) {
        val item = westCache.get(option, cacheKey, callable);
        if (item.isExpiredAfterWrite()) {
            westCache.invalidate(option, cacheKey, null);
        }
        return westCache.get(option, cacheKey, callable);
    }

    @Override
    public WestCacheItem getIfPresent(WestCacheOption option,
                                      String cacheKey) {
        val item = westCache.getIfPresent(option, cacheKey);
        if (item == null) return null;
        if (!item.isExpiredAfterWrite()) return item;

        westCache.invalidate(option, cacheKey, null);
        return null;
    }

    @Override
    public void put(WestCacheOption option,
                    String cacheKey,
                    WestCacheItem cacheValue) {
        westCache.put(option, cacheKey, cacheValue);
    }

    @Override
    public void invalidate(WestCacheOption option,
                           String cacheKey,
                           String version) {
        westCache.invalidate(option, cacheKey, version);
    }
}
