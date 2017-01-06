package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.Durations;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Redis;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisCommands;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.isBlank;

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
    private static class RedisWestCache implements WestCache {
        String prefix;

        public RedisWestCache() {
            this(Redis.PREFIX);
        }

        @Override @SneakyThrows
        public WestCacheItem get(WestCacheOption option,
                                 String cacheKey,
                                 Callable<WestCacheItem> callable) {
            val redis = Redis.getRedis(option);
            String redisKey = prefix + cacheKey;
            val jsonValue1 = redis.get(redisKey);
            if (jsonValue1 != null) {
                val object = FastJsons.parse(jsonValue1, option.getMethod());
                return new WestCacheItem(object);
            }

            val lockKey = prefix + "lock:" + cacheKey;
            Redis.waitRedisLock(redis, lockKey);
            log.debug("got redis lock {} for get", lockKey);

            @Cleanup val i = new Closeable() {
                @Override public void close() throws IOException {
                    redis.del(lockKey);
                    log.debug("del redis lock {} for get", lockKey);
                }
            }; // free lock automatically

            val jsonValue2 = redis.get(redisKey);
            if (jsonValue2 != null) {
                val object = FastJsons.parse(jsonValue2, option.getMethod());
                return new WestCacheItem(object);
            }

            val item = callable.call();
            put(option, cacheKey, item);
            return item;
        }

        @Override
        public WestCacheItem getIfPresent(WestCacheOption option,
                                          String cacheKey) {
            val jsonValue = Redis.getRedis(option).get(prefix + cacheKey);
            if (StringUtils.isNotEmpty(jsonValue)) {
                val object = FastJsons.parse(jsonValue, option.getMethod());
                return new WestCacheItem(object);
            }

            return new WestCacheItem(null);
        }

        @Override
        public void put(WestCacheOption option,
                        String cacheKey,
                        WestCacheItem cacheValue) {
            val redis = Redis.getRedis(option);
            val key = prefix + cacheKey;
            if (cacheValue != null) {
                val json = FastJsons.json(cacheValue.getObject().get());
                expirePut(option, redis, key, json);
            } else {
                redis.del(key);
            }
        }

        @Override @SneakyThrows
        public void invalidate(WestCacheOption option,
                               String cacheKey,
                               String version) {
            val redis = Redis.getRedis(option);

            val redisKey = prefix + cacheKey;
            if (StringUtils.isEmpty(version)) {
                redis.del(redisKey);
                return;
            }

            val lockKey = prefix + "lock:" + cacheKey;
            Redis.waitRedisLock(redis, lockKey);
            log.debug("got redis lock {} for invalidate", lockKey);

            @Cleanup val i = new Closeable() {
                @Override public void close() throws IOException {
                    redis.del(lockKey);
                    log.debug("del redis lock {} for invalidate", lockKey);
                }
            }; // free lock automatically

            val versionKey = prefix + "version:" + cacheKey;
            val versionRedis = redis.get(versionKey);
            if (version.equals(versionRedis)) return;

            redis.del(redisKey);
            redis.set(versionKey, version);
        }
    }

    private static String expirePut(WestCacheOption option,
                                    JedisCommands redis,
                                    String key,
                                    String json) {
        val expireKey = "expireAfterWrite";
        val expireWrite = option.getSpecs().get(expireKey);
        if (isBlank(expireWrite)) return redis.set(key, json);

        val duration = Durations.parse(expireKey, expireWrite, SECONDS);
        log.info("redis set {}={} in expire {} seconds", key, json, duration);
        return redis.set(key, json, "NX", "EX", duration);
    }
}
