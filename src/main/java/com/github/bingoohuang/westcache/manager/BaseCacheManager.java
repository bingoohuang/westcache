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
    private WestCache westCache;

    public BaseCacheManager(WestCache westCache) {
        this.westCache = westCache;
    }

    @Override @SuppressWarnings("unchecked") @SneakyThrows
    public Optional<Object> get(final WestCacheOption option,
                                final String cacheKey,
                                final Callable<Optional<Object>> callable) {
        val flusher = option.getFlusher();
        if (!flusher.isKeyEnabled(option, cacheKey)) {
            log.info("cache key {} is not enabled", cacheKey);
            return callable.call();
        }

        flusher.register(option, cacheKey, westCache);

        val flushCallable = new Callable<Optional<Object>>() {
            @Override public Optional<Object> call() throws Exception {
                Object raw = flusher.getDirectValue(option, cacheKey);
                if (raw != null) return Optional.fromNullable(raw);

                return callable.call();
            }
        };

        Callable<Optional<Object>> wrapCallable = new Callable<Optional<Object>>() {
            @Override public Optional<Object> call() throws Exception {
                return option.getSnapshot() == null ? flushCallable.call()
                        : trySnapshot(option, cacheKey, flushCallable);
            }
        };
        return westCache.get(cacheKey, wrapCallable);
    }

    @SneakyThrows
    private Optional<Object> trySnapshot(final WestCacheOption option,
                                         final String cacheKey,
                                         final Callable<Optional<Object>> callable) {
        val future = option.getConfig().executorService().submit(new Callable<Optional<Object>>() {
            @Override public Optional<Object> call() throws Exception {
                val optional = callable.call();
                westCache.put(cacheKey, optional);
                option.getSnapshot().saveSnapshot(option, cacheKey, optional.orNull());
                return optional;
            }
        });

        long timeout = option.getConfig().timeoutMillisToSnapshot();
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.info("get cache {} timeout in {} millis, try snapshot", timeout, cacheKey);
            Optional<Object> result = option.getSnapshot().readSnapshot(option, cacheKey);
            log.info("got {} snapshot {}", cacheKey, result != null ? result.orNull() : " non-exist");
            return result != null ? result : future.get();
        }
    }

    @Override
    public Optional<Object> get(WestCacheOption option, String cacheKey) {
        return westCache.getIfPresent(cacheKey);
    }
}
