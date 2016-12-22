package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheManager;
import com.github.bingoohuang.westcache.impl.WestCacheOption;
import com.google.common.base.Optional;
import lombok.experimental.UtilityClass;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@UtilityClass
public class WestCacheGuava {
    WestCacheManager manager = WestCacheManagerBuilder.buildGuavaCacheManager();

    public <T> Optional<T> get(WestCacheOption option, String cacheKey, Callable<Optional<T>> callable) {
        return manager.get(option, cacheKey, callable);
    }

    public <T> Optional<T> get(WestCacheOption option, String cacheKey) {
        return manager.get(option, cacheKey);
    }

    public <T> Optional<T> getSnapshot(WestCacheOption option, String cacheKey, Callable<Optional<T>> callable) {
        return manager.getSnapshot(option, cacheKey, callable);
    }

    public <T> void put(WestCacheOption option, String cacheKey, Optional<T> cacheValue) {
        manager.put(option, cacheKey, cacheValue);
    }
}
