package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.Durations;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import net.jodah.expiringmap.ExpiringMap;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.jodah.expiringmap.ExpirationPolicy.ACCESSED;
import static net.jodah.expiringmap.ExpirationPolicy.CREATED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/2.
 */
public class ExpiringMapCacheManager extends BaseCacheManager {
    public ExpiringMapCacheManager() {
        super(new ExpiringMapCache());
    }

    private static class ExpiringMapCache implements WestCache {
        static WestCacheItem lockItem = new WestCacheItem(null);
        final ExpiringMap<String, WestCacheItem> cache =
                ExpiringMap.builder()
                        .variableExpiration()
                        .build();

        @Override @SneakyThrows
        public WestCacheItem get(WestCacheOption option,
                                 String cacheKey,
                                 Callable<WestCacheItem> callable) {
            WestCacheItem cacheItem = cache.get(cacheKey);
            if (cacheItem != null) return cacheItem;

            val lockKey = cacheKey + ":lock";
            while (!lockCacheKey(lockKey)) Thread.sleep(100);
            @Cleanup val i = new Closeable() {
                @Override public void close() throws IOException {
                    cache.remove(lockKey);
                }
            };
            cacheItem = cache.get(cacheKey);
            if (cacheItem != null) return cacheItem;

            cacheItem = callable.call();
            putItem(cache, option, cacheKey, cacheItem);

            return cacheItem;
        }

        private boolean lockCacheKey(String lockKey) {
            Object prev = cache.putIfAbsent(lockKey, lockItem);
            return prev == null;
        }

        @Override
        public WestCacheItem getIfPresent(WestCacheOption option,
                                          String cacheKey) {
            return cache.get(cacheKey);
        }

        @Override
        public void put(WestCacheOption option,
                        String cacheKey,
                        WestCacheItem cacheItem) {
            cache.put(cacheKey, cacheItem);
        }

        @Override
        public void invalidate(WestCacheOption option, String cacheKey) {
            cache.remove(cacheKey);
        }
    }

    private static void putItem(ExpiringMap<String, WestCacheItem> cache,
                                WestCacheOption option,
                                String cacheKey,
                                WestCacheItem cacheItem) {
        // expireAfterAccess=[duration];expireAfterWrite=[duration];
        // Durations are represented by an integer,
        // followed by one of "d", "h", "m", or "s",
        // representing days, hours, minutes, or seconds respectively.
        String keyA = "expireAfterAccess";
        String expireAccess = option.getSpecs().get(keyA);
        String keyW = "expireAfterWrite";
        String expireWrite = option.getSpecs().get(keyW);
        if (isNotBlank(expireAccess)) {
            val duration = Durations.parse(keyA, expireAccess, SECONDS);
            cache.put(cacheKey, cacheItem, ACCESSED, duration, SECONDS);
        } else if (isNotBlank(expireWrite)) {
            val duration = Durations.parse(keyW, expireWrite, SECONDS);
            cache.put(cacheKey, cacheItem, CREATED, duration, SECONDS);
        } else {
            cache.put(cacheKey, cacheItem);
        }
    }
}
