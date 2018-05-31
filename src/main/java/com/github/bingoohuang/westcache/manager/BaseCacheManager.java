package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.base.WestCacheManager;
import com.github.bingoohuang.westcache.utils.Envs;
import com.github.bingoohuang.westcache.utils.MethodProvider;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@Slf4j
public abstract class BaseCacheManager implements WestCacheManager {
    private final long startupTime = System.currentTimeMillis();

    @Getter private WestCache westCache;
    private Method longMethod = init();

    @SneakyThrows
    private Method init() {
        return MethodProvider.class.getMethod("longMethod");
    }

    public BaseCacheManager(WestCache westCache) {
        this.westCache = westCache;
    }

    @Override @SuppressWarnings("unchecked")
    public WestCacheItem get(final WestCacheOption option,
                             final String cacheKey,
                             final Callable<WestCacheItem> callable) {
        val flusher = option.getFlusher();
        if (!flusher.isKeyEnabled(option, cacheKey)) {
            log.warn("cache key {} is not enabled", cacheKey);
            return Envs.execute(callable);
        }

        flusher.register(option, cacheKey, westCache);

        val shot = new AtomicBoolean(true);
        Callable<WestCacheItem> flushCallable = () -> {
            val raw = flusher.getDirectValue(option, cacheKey);
            if (raw.isPresent()) return new WestCacheItem(raw, option);

            shot.set(false);
            val interceptor = option.getInterceptor();
            return interceptor.intercept(option, cacheKey, callable);
        };

        Callable<WestCacheItem> wrapCallable = () -> option.getSnapshot() == null
                ? Envs.execute(flushCallable)
                : trySnapshot(option, cacheKey, flushCallable);

        checkStartupTimeValidate(option, cacheKey);

        val item = westCache.get(option, cacheKey, wrapCallable);
        log.debug("cache key {} shot result {} ", cacheKey,
                shot.get() ? "bingo" : "misfired");

        return item;
    }

    private void checkStartupTimeValidate(WestCacheOption option, String cacheKey) {
        if (!"true".equals(option.getSpecs().get("restartInvalidate"))) return;

        val cloneOption = WestCacheOption.builder().clone(option).method(longMethod).build();
        val startupTimeKey = "startupTime:" + cacheKey;
        val timeItem = westCache.getIfPresent(cloneOption, startupTimeKey);

        val validate = timeItem.isPresent() && (Long) timeItem.orNull() >= startupTime;
        if (!validate) {
            westCache.invalidate(option, cacheKey, "");
            westCache.put(option, startupTimeKey, new WestCacheItem(Optional.of(startupTime), option));
        }
    }

    private WestCacheItem trySnapshot(final WestCacheOption option,
                                      final String cacheKey,
                                      final Callable<WestCacheItem> callable) {
        val executorService = Executors.newSingleThreadScheduledExecutor();
        Future<WestCacheItem> future = executorService.submit(() -> {
            val item = Envs.execute(callable);
            westCache.put(option, cacheKey, item);
            val snapshot = option.getSnapshot();
            snapshot.saveSnapshot(option, cacheKey, item);
            return item;
        });

        return Envs.trySnapshot(option, future, cacheKey);
    }

    @Override
    public WestCacheItem get(WestCacheOption option, String cacheKey) {
        return westCache.getIfPresent(option, cacheKey);
    }

    @Override
    public void put(WestCacheOption option, String cacheKey, WestCacheItem cacheValue) {
        westCache.put(option, cacheKey, cacheValue);
    }

    @Override
    public void invalidate(WestCacheOption option, String cacheKey, String version) {
        westCache.invalidate(option, cacheKey, version);
    }
}
