package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheManager;
import com.google.common.base.Optional;
import lombok.experimental.UtilityClass;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@UtilityClass
public class WestCacheGuava {
    WestCacheManager manager = WestCacheManagerBuilder.buildGuavaCacheManager();

    public <T> Optional<T> get(String cacheKey, Callable<Optional<T>> callable) {
        return manager.get(cacheKey, callable);
    }

    public <T> Optional<T> get(String cacheKey) {
        return manager.get(cacheKey);
    }

    public <T> Optional<T> getSnapshot(String cacheKey, Callable<Optional<T>> callable) {
        return manager.getSnapshot(cacheKey, callable);
    }

    public <T> void put(String cacheKey, Optional<T> cacheValue) {
        manager.put(cacheKey, cacheValue);
    }
}
