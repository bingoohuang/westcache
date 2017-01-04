package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Redis;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public class RedisCacheManager extends BaseCacheManager {
    public RedisCacheManager(String prefix) {
        super(new RedisWestCache(prefix));
    }

    public RedisCacheManager() {
        super(new RedisWestCache());
    }

    @AllArgsConstructor
    private static class RedisWestCache implements WestCache {
        String prefix;

        public RedisWestCache() {
            this(Redis.PREFIX);
        }

        @Override @SneakyThrows
        public WestCacheItem get(WestCacheOption option,
                                 String cacheKey,
                                 Callable<WestCacheItem> callable) {
            String jsonValue = Redis.getRedis(option).get(prefix + cacheKey);
            if (StringUtils.isNotEmpty(jsonValue)) {
                Object object = FastJsons.parse(jsonValue);
                return new WestCacheItem(object);
            }

            val item = callable.call();
            put(option, cacheKey, item);
            return item;
        }

        @Override
        public WestCacheItem getIfPresent(WestCacheOption option,
                                          String cacheKey) {
            String jsonValue = Redis.getRedis(option).get(prefix + cacheKey);
            if (StringUtils.isNotEmpty(jsonValue)) {
                Object object = FastJsons.parse(jsonValue);
                return new WestCacheItem(object);
            }

            return new WestCacheItem(null);
        }

        @Override
        public void put(WestCacheOption option,
                        String cacheKey,
                        WestCacheItem cacheValue) {
            if (cacheValue != null) {
                val json = FastJsons.json(cacheValue.getObject().get());
                Redis.getRedis(option).set(prefix + cacheKey, json);
            } else {
                Redis.getRedis(option).del(prefix + cacheKey);
            }
        }

        @Override
        public void invalidate(WestCacheOption option, String cacheKey) {
            Redis.getRedis(option).del(prefix + cacheKey);
        }
    }
}
