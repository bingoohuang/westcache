package com.github.bingoohuang.westcache.cachemanager;

import com.github.bingoohuang.westcache.WestCache;
import com.github.bingoohuang.westcache.WestCacheManager;
import com.github.bingoohuang.westcache.WestCacheSnapshot;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@Slf4j
public abstract class AbstractCacheManager implements WestCacheManager {
    protected abstract Cache<String, Object> getCache();

    protected abstract ExecutorService getExecutorService();

    protected abstract WestCacheSnapshot getCacheSnapshot();

    @Override @SneakyThrows
    public <T> Optional<T> get(String cacheKey, Callable<Optional<T>> callable) {
        return (Optional<T>) getCache().get(cacheKey, callable);
    }

    @Override @SneakyThrows
    public <T> Optional<T> getSnapshot(final String cacheKey,
                                       final Callable<Optional<T>> callable) {
        val future = getExecutorService().submit(new Callable<Optional<T>>() {
            @Override
            public Optional<T> call() throws Exception {
                val optional = callable.call();
                WestCache.put(cacheKey, optional);
                getCacheSnapshot().saveSnapshot(cacheKey, optional.orNull());
                return optional;
            }
        });

        Optional<T> result;
        try {
            result = future.get(500, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {   // 有限时间内不返回，尝试snapshot
            log.info("get cache {} timeout in 500 millis, try to use snapshot", cacheKey);
            result = getCacheSnapshot().readSnapShot(cacheKey);
        }

        return result == null ? future.get() : result;
    }

    @Override public <T> Optional<T> get(String cacheKey) {
        return (Optional<T>) getCache().getIfPresent(cacheKey);
    }

    @Override public <T> void put(String cacheKey, Optional<T> cacheValue) {
        getCache().put(cacheKey, cacheValue);
    }
}
