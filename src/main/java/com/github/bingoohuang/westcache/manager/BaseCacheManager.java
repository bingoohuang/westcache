package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheManager;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
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

    @Override @SuppressWarnings("unchecked") @SneakyThrows
    public <T> Optional<T> get(final WestCacheOption option,
                               final String cacheKey,
                               final Callable<Optional<T>> callable) {
        val flusher = option.getFlusher();
        if (!flusher.isKeyEnabled(option, cacheKey)) {
            log.info("cache key {} is not enabled", cacheKey);
            return callable.call();
        }

        flusher.register(option, cacheKey, getWestCache());

        val wrapCallable = new Callable<Optional<T>>() {
            @Override public Optional<T> call() throws Exception {
                return option.getSnapshot() == null ? callable.call()
                        : trySnapshot(option, cacheKey, callable);
            }
        };
        return (Optional<T>) getWestCache().get(cacheKey, wrapCallable);
    }

    @SneakyThrows
    private <T> Optional<T> trySnapshot(final WestCacheOption option,
                                        final String cacheKey,
                                        final Callable<Optional<T>> callable) {
        val future = option.getConfig().executorService().submit(new Callable<Optional<T>>() {
            @Override public Optional<T> call() throws Exception {
                val optional = callable.call();
                getWestCache().put(cacheKey, optional);
                option.getSnapshot().saveSnapshot(option, cacheKey, optional.orNull());
                return optional;
            }
        });

        long timeout = option.getConfig().timeoutMillisToSnapshot();
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {   // 有限时间内不返回，尝试snapshot
            log.info("get cache {} timeout in {} millis, try to use snapshot", timeout, cacheKey);
            Optional<T> result = option.getSnapshot().readSnapshot(option, cacheKey);
            log.info("got {} snapshot {}", cacheKey, result != null ? result.orNull() : " not exist");
            return result == null ? future.get() : result;
        }
    }

    @Override
    public <T> Optional<T> get(WestCacheOption option, String cacheKey) {
        return (Optional<T>) getWestCache().getIfPresent(cacheKey);
    }
}
