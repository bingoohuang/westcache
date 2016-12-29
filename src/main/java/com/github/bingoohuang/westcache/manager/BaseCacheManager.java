package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.base.WestCacheManager;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

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
    public WestCacheItem get(final WestCacheOption option,
                             final String cacheKey,
                             final Callable<WestCacheItem> callable) {
        val flusher = option.getFlusher();
        if (!flusher.isKeyEnabled(option, cacheKey)) {
            log.info("cache key {} is not enabled", cacheKey);
            return callable.call();
        }

        flusher.register(option, cacheKey, westCache);

        val shot = new AtomicBoolean(true);
        val flushCallable = new Callable<WestCacheItem>() {
            @Override public WestCacheItem call() throws Exception {
                Object raw = flusher.getDirectValue(option, cacheKey);
                if (raw != null) return new WestCacheItem(raw);

                shot.set(false);
                return callable.call();
            }
        };

        Callable<WestCacheItem> wrapCallable = new Callable<WestCacheItem>() {
            @Override public WestCacheItem call() throws Exception {
                return option.getSnapshot() == null ? flushCallable.call()
                        : trySnapshot(option, cacheKey, flushCallable);
            }
        };
        WestCacheItem item = westCache.get(option, cacheKey, wrapCallable);

        log.info("cache key {} shot result {} ", cacheKey, shot.get() ? "bingo" : "misfired");

        return item;
    }

    @SneakyThrows
    private WestCacheItem trySnapshot(final WestCacheOption option,
                                      final String cacheKey,
                                      final Callable<WestCacheItem> callable) {
        val future = option.getConfig().executorService().submit(new Callable<WestCacheItem>() {
            @Override public WestCacheItem call() throws Exception {
                val item = callable.call();
                westCache.put(option, cacheKey, item);
                option.getSnapshot().saveSnapshot(option, cacheKey, item);
                return item;
            }
        });

        long timeout = option.getConfig().timeoutMillisToSnapshot();
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.info("get cache {} timeout in {} millis, try snapshot", timeout, cacheKey);
            WestCacheItem result = option.getSnapshot().readSnapshot(option, cacheKey);
            log.info("got {} snapshot {}", cacheKey, result != null ? result.getObject() : " non-exist");
            return result != null ? result : future.get();
        }
    }

    @Override
    public WestCacheItem get(WestCacheOption option, String cacheKey) {
        return westCache.getIfPresent(option, cacheKey);
    }

    @Override
    public void put(WestCacheOption option,
                    String cacheKey,
                    WestCacheItem cacheValue) {
        westCache.put(option, cacheKey, cacheValue);
    }
}
