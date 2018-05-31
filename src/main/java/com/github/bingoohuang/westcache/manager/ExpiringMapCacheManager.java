package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.Envs;
import com.github.bingoohuang.westcache.utils.QuietCloseable;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import lombok.Cleanup;
import lombok.val;
import net.jodah.expiringmap.ExpiringMap;

import java.util.concurrent.Callable;

import static com.github.bingoohuang.westcache.utils.Durations.parse;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.jodah.expiringmap.ExpirationPolicy.ACCESSED;
import static net.jodah.expiringmap.ExpirationPolicy.CREATED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/2.
 */
public class ExpiringMapCacheManager extends BaseCacheManager {
    public ExpiringMapCacheManager() {
        super(new ExpiringCache());
    }

    public static class ExpiringCache implements WestCache {
        static WestCacheItem lockItem = new WestCacheItem(Optional.absent(), null);
        protected final ExpiringMap<String, WestCacheItem> cache =
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
            @Cleanup QuietCloseable i = () -> cache.remove(lockKey);
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

        /**
         * expireAfterAccess=[duration];expireAfterWrite=[duration];
         * Durations are represented by an integer,
         * followed by one of "d", "h", "m", or "s",
         * representing days, hours, minutes, or seconds respectively.
         */
        protected void putItem(WestCacheOption option,
                               String cacheKey,
                               WestCacheItem item) {
            val expireWrite = item.getDurationSeconds();
            val expireAccess = option.getSpecs().get("expireAfterAccess");
            if (expireWrite != 0) {
                cache.put(cacheKey, item, CREATED, expireWrite, SECONDS);
            } else if (isNotBlank(expireAccess)) {
                val duration = parse("expireAfterAccess", expireAccess);
                cache.put(cacheKey, item, ACCESSED, duration, SECONDS);
            } else {
                cache.put(cacheKey, item);
            }
        }
    }
}
