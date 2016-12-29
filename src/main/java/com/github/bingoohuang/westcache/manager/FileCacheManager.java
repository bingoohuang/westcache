package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.snapshot.FileCacheSnapshot;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import lombok.SneakyThrows;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public class FileCacheManager extends BaseCacheManager {
    static class FileWestCache implements WestCache {
        FileCacheSnapshot snapshot = new FileCacheSnapshot();

        @Override @SneakyThrows
        public Optional<Object> get(WestCacheOption option, String cacheKey, Callable<Optional<Object>> callable) {
            Optional<Object> optional = snapshot.readSnapshot(option, cacheKey);
            if (optional != null) return optional;

            Optional<Object> result = callable.call();
            put(option, cacheKey, result);

            return result;
        }

        @Override
        public Optional<Object> getIfPresent(WestCacheOption option, String cacheKey) {
            return snapshot.readSnapshot(option, cacheKey);
        }

        @Override
        public void put(WestCacheOption option, String cacheKey, Optional<Object> cacheValue) {
            if (cacheValue == null) return;
            snapshot.saveSnapshot(option, cacheKey, cacheValue.get());
        }

        @Override
        public void invalidate(WestCacheOption option, String cacheKey) {
            snapshot.deleteSnapshot(option, cacheKey);
        }
    }

    public FileCacheManager() {
        super(new FileWestCache());
    }
}
