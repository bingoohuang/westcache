package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.Guavas;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public class GuavaCacheManager extends BaseCacheManager {
    public GuavaCacheManager() {
        super(new ExpireAfterWritableWestCacheAdaptor(new GuavaWestCache()));
    }

    public static class GuavaWestCache implements WestCache {
        private Cache<String, WestCacheItem> cache
                = CacheBuilder.newBuilder().build();

        @Override
        public WestCacheItem get(WestCacheOption option,
                                 String cacheKey,
                                 Callable<WestCacheItem> callable) {
            return Guavas.cacheGet(cache, cacheKey, callable);
        }

        @Override
        public WestCacheItem getIfPresent(WestCacheOption option,
                                          String cacheKey) {
            return cache.getIfPresent(cacheKey);
        }

        @Override
        public void put(WestCacheOption option,
                        String cacheKey,
                        WestCacheItem cacheValue) {
            cache.put(cacheKey, cacheValue);
        }

        @Override
        public void invalidate(WestCacheOption option,
                               String cacheKey, String version) {
            cache.invalidate(cacheKey);
        }
    }

}
