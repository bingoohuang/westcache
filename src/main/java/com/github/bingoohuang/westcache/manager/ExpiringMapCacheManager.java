package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.Durations;
import com.github.bingoohuang.westcache.utils.Envs;
import com.github.bingoohuang.westcache.utils.QuietCloseable;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.Cleanup;
import lombok.val;
import net.jodah.expiringmap.ExpiringMap;

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

    public static class ExpiringMapCache implements WestCache {
        static WestCacheItem lockItem = new WestCacheItem(null);
        final protected ExpiringMap<String, WestCacheItem> cache =
                ExpiringMap.builder()
                        .variableExpiration()
                        .build();

        @Override
        public WestCacheItem get(WestCacheOption option,
                                 String cacheKey,
                                 Callable<WestCacheItem> callable) {
            val cacheItem1 = getIfPresent(option, cacheKey);
            if (cacheItem1 != null) return cacheItem1;

            val lockKey = cacheKey + ":lock";
            while (!lockCacheKey(lockKey)) Envs.sleepMillis(100L);
            @Cleanup val i = new QuietCloseable() {
                @Override public void close() {
                    cache.remove(lockKey);
                }
            };
            val cacheItem2 = cache.get(cacheKey);
            if (cacheItem2 != null) return cacheItem2;

            val cacheItem3 = Envs.execute(callable);
            putItem(option, cacheKey, cacheItem3);

            return cacheItem3;
        }

        private boolean lockCacheKey(String lockKey) {
            val prev = cache.putIfAbsent(lockKey, lockItem);
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
        public void invalidate(WestCacheOption option,
                               String cacheKey,
                               String version) {
            cache.remove(cacheKey);
        }

        protected void putItem(WestCacheOption option,
                               String cacheKey,
                               WestCacheItem cacheItem) {
            // expireAfterAccess=[duration];expireAfterWrite=[duration];
            // Durations are represented by an integer,
            // followed by one of "d", "h", "m", or "s",
            // representing days, hours, minutes, or seconds respectively.
            val keyW = "expireAfterWrite";
            val keyA = "expireAfterAccess";
            val expireWrite = option.getSpecs().get(keyW);
            val expireAccess = option.getSpecs().get(keyA);
            if (isNotBlank(expireWrite)) {
                val duration = Durations.parse(keyW, expireWrite, SECONDS);
                cache.put(cacheKey, cacheItem, CREATED, duration, SECONDS);
            } else if (isNotBlank(expireAccess)) {
                val duration = Durations.parse(keyA, expireAccess, SECONDS);
                cache.put(cacheKey, cacheItem, ACCESSED, duration, SECONDS);
            } else {
                cache.put(cacheKey, cacheItem);
            }
        }
    }


}
