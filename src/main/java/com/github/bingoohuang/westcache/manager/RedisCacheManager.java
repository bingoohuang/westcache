package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.interceptor.RedisInterceptor;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.QuietCloseable;
import com.github.bingoohuang.westcache.utils.Redis;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisCommands;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
@Slf4j
public class RedisCacheManager extends BaseCacheManager {
    public RedisCacheManager(String prefix) {
        super(new RedisWestCache(prefix));
    }

    public RedisCacheManager() {
        super(new RedisWestCache());
    }

    @AllArgsConstructor
    public static class RedisWestCache implements WestCache {
        String prefix;

        public RedisWestCache() {
            this(Redis.PREFIX);
        }

        @Override
        public WestCacheItem get(
                WestCacheOption option, String cacheKey,
                Callable<WestCacheItem> callable) {
            return new RedisInterceptor().intercept(option, cacheKey, callable);
        }

        @Override
        public WestCacheItem getIfPresent(WestCacheOption option, String cacheKey) {
            val json = Redis.getRedis(option).get(prefix + cacheKey);
            if (StringUtils.isNotEmpty(json)) {
                val method = option.getMethod();
                val object = FastJsons.parse(json, method, true);
                val optional = Optional.fromNullable(object);
                return new WestCacheItem(optional, option);
            }

            return new WestCacheItem(Optional.absent(), option);
        }

        @Override
        public void put(WestCacheOption option, String cacheKey,
                        WestCacheItem cacheValue) {
            val redis = Redis.getRedis(option);
            val key = prefix + cacheKey;
            Redis.expirePut(option, redis, key, cacheValue);
        }

        @Override
        public void invalidate(
                WestCacheOption option, String cacheKey, String version) {
            val redis = Redis.getRedis(option);

            val redisKey = prefix + cacheKey;
            if (StringUtils.isEmpty(version)) {
                redis.del(redisKey);
                return;
            }

            if (!"true".equals(option.getSpecs().get("redisLockFirst"))) return;

            val lockKey = prefix + "lock:" + cacheKey;
            val locked = Redis.waitRedisLock(redis, lockKey);
            log.debug("got redis lock {}={} for invalidate", lockKey, locked);

            @Cleanup QuietCloseable i = () -> {
                redis.del(lockKey);
                log.debug("del redis lock {} for invalidate", lockKey);
            }; // free lock automatically

            setVersionToRedis(cacheKey, version, redis, redisKey);
        }

        private void setVersionToRedis(String cacheKey, String version,
                                       JedisCommands redis, String redisKey) {
            val versionKey = prefix + "version:" + cacheKey;
            val versionRedis = redis.get(versionKey);
            if (version.equals(versionRedis)) return;

            redis.del(redisKey);
            redis.set(versionKey, version);
        }
    }
}
