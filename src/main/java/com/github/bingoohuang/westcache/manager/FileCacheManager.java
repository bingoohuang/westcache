package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.snapshot.FileCacheSnapshot;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.SneakyThrows;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public class FileCacheManager extends BaseCacheManager {
    static class FileWestCache implements WestCache {
        FileCacheSnapshot snapshot = new FileCacheSnapshot();

        @Override @SneakyThrows
        public WestCacheItem get(WestCacheOption option,
                                 String cacheKey,
                                 Callable<WestCacheItem> callable) {
            WestCacheItem item = snapshot.readSnapshot(option, cacheKey);
            if (item != null) return item;

            WestCacheItem result = callable.call();
            put(option, cacheKey, result);

            return result;
        }

        @Override
        public WestCacheItem getIfPresent(WestCacheOption option,
                                          String cacheKey) {
            return snapshot.readSnapshot(option, cacheKey);
        }

        @Override
        public void put(WestCacheOption option,
                        String cacheKey,
                        WestCacheItem cacheValue) {
            if (cacheValue == null) return;

            snapshot.saveSnapshot(option, cacheKey, cacheValue);
        }

        @Override
        public void invalidate(WestCacheOption option, String cacheKey, String version) {
            snapshot.deleteSnapshot(option, cacheKey);
        }
    }

    public FileCacheManager() {
        super(new FileWestCache());
    }
}
