package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.WestCacheGuava;
import com.github.bingoohuang.westcache.base.WestCacheManager;
import com.github.bingoohuang.westcache.base.WestCacheSnapshot;
import com.google.common.base.Optional;
import lombok.Getter;
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
public class BaseCacheManager implements WestCacheManager {
    @Getter private WestCache<String, Object> cache;
    @Getter private ExecutorService executorService;
    @Getter private WestCacheSnapshot westCacheSnapshot;

    public BaseCacheManager(WestCache<String, Object> cache,
                            ExecutorService executorService,
                            WestCacheSnapshot westCacheSnapshot) {
        this.cache = cache;
        this.executorService = executorService;
        this.westCacheSnapshot = westCacheSnapshot;
    }

    @Override @SneakyThrows @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String cacheKey, Callable<Optional<T>> callable) {
        return (Optional<T>) cache.get(cacheKey, callable);
    }

    @Override @SneakyThrows
    public <T> Optional<T> getSnapshot(final String cacheKey,
                                       final Callable<Optional<T>> callable) {
        val future = executorService.submit(new Callable<Optional<T>>() {
            @Override
            public Optional<T> call() throws Exception {
                val optional = callable.call();
                WestCacheGuava.put(cacheKey, optional);
                westCacheSnapshot.saveSnapshot(cacheKey, optional.orNull());
                return optional;
            }
        });

        Optional<T> result;
        try {
            result = future.get(500, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {   // 有限时间内不返回，尝试snapshot
            log.info("get cache {} timeout in 500 millis, try to use snapshot", cacheKey);
            result = westCacheSnapshot.readSnapShot(cacheKey);
        }

        return result == null ? future.get() : result;
    }

    @Override public <T> Optional<T> get(String cacheKey) {
        return (Optional<T>) cache.getIfPresent(cacheKey);
    }

    @Override public <T> void put(String cacheKey, Optional<T> cacheValue) {
        cache.put(cacheKey, cacheValue);
    }
}
