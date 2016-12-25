package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.utils.WestCacheOptions;
import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheManager;
import com.google.common.base.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@Slf4j
public abstract class BaseCacheManager implements WestCacheManager {
    public abstract WestCache<String, Object> getWestCache();

    @Override @SuppressWarnings("unchecked")
    public <T> Optional<T> get(final WestCacheOptions option,
                               final String cacheKey,
                               final Callable<Optional<T>> callable) {
        if (option.getSnapshot() == null) {
            return (Optional<T>) getWestCache().get(cacheKey, new Callable<Optional<T>>() {
                @Override public Optional<T> call() throws Exception {
                    option.getFlusher().register(cacheKey, getWestCache());
                    Optional<T> call = callable.call();
                    return call;
                }
            });
        } else {
            return (Optional<T>) getWestCache().get(cacheKey, new Callable<Optional<T>>() {
                @Override
                public Optional<T> call() throws Exception {
                    return trySnapshot(option, cacheKey, callable);
                }
            });
        }
    }

    @SneakyThrows
    private <T> Optional<T> trySnapshot(final WestCacheOptions option,
                                        final String cacheKey,
                                        final Callable<Optional<T>> callable) {
        val future = option.getConfig().executorService().submit(new Callable<Optional<T>>() {
            @Override
            public Optional<T> call() throws Exception {
                option.getFlusher().register(cacheKey, getWestCache());
                val optional = callable.call();
                put(option, cacheKey, optional);
                option.getSnapshot().saveSnapshot(cacheKey, optional.orNull());
                return optional;
            }
        });

        Optional<T> result;
        long timeout = option.getConfig().timeoutMillisToSnapshot();
        try {
            result = future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {   // 有限时间内不返回，尝试snapshot
            log.info("get cache {} timeout in {} millis, try to use snapshot", timeout, cacheKey);
            result = option.getSnapshot().readSnapshot(cacheKey);
            log.info("got {} snapshot {}", cacheKey, result != null ? result.orNull() : " not exist");
        }

        return result == null ? future.get() : result;
    }

    @Override
    public <T> Optional<T> get(WestCacheOptions option, String cacheKey) {
        return (Optional<T>) getWestCache().getIfPresent(cacheKey);
    }

    @Override
    public <T> void put(WestCacheOptions option,
                        String cacheKey,
                        Optional<T> cacheValue) {
        option.getFlusher().register(cacheKey, getWestCache());
        getWestCache().put(cacheKey, cacheValue);
    }
}
