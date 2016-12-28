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

        val flushCallable = new Callable<Optional<T>>() {
            @Override public Optional<T> call() throws Exception {
                T raw =  flusher.getDirectValue(option, cacheKey);
                if (raw != null) return Optional.fromNullable(raw);

                return callable.call();
            }
        };

        val wrapCallable = new Callable<Optional<T>>() {
            @Override public Optional<T> call() throws Exception {
                return option.getSnapshot() == null ? flushCallable.call()
                        : trySnapshot(option, cacheKey, flushCallable);
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
        } catch (TimeoutException e) {
            log.info("get cache {} timeout in {} millis, try snapshot", timeout, cacheKey);
            Optional<T> result = option.getSnapshot().readSnapshot(option, cacheKey);
            log.info("got {} snapshot {}", cacheKey, result != null ? result.orNull() : " non-exist");
            return result != null ? result : future.get();
        }
    }

    @Override
    public <T> Optional<T> get(WestCacheOption option, String cacheKey) {
        return (Optional<T>) getWestCache().getIfPresent(cacheKey);
    }
}
