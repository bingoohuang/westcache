package com.github.bingoohuang.westcache.interceptor;

import com.github.bingoohuang.westcache.base.WestCacheInterceptor;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.Durations;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Redis;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/3.
 */
@Slf4j
public class RedisInterceptor implements WestCacheInterceptor {
    @Override
    public WestCacheItem intercept(
            final WestCacheOption option,
            String cacheKey,
            Callable<WestCacheItem> callable) throws Exception {
        val redisValueKey = Redis.PREFIX + cacheKey;
        val redis = Redis.getRedis(option);
        val redisValue1 = redis.get(redisValueKey);
        if (redisValue1 != null) {
            log.debug("got redis value {}", redisValue1);
            Object value = FastJsons.parse(redisValue1, option.getMethod());
            return new WestCacheItem(value);
        }

        final val lockKey = Redis.PREFIX + "lock:" + cacheKey;
        log.debug("wait redis lock {}", lockKey);
        Redis.waitRedisLock(redis, lockKey);
        log.debug("got redis lock {}", lockKey);
        @Cleanup val i = new Closeable() {
            @Override public void close() throws IOException {
                redis.del(lockKey);
                log.debug("del redis lock {}", lockKey);
            }
        }; // free lock automatically

        val redisValue2 = redis.get(redisValueKey);
        if (redisValue2 != null) {
            log.debug("got redis value {}", redisValue2);
            Object value = FastJsons.parse(redisValue2, option.getMethod());
            return new WestCacheItem(value);
        }

        WestCacheItem item = callable.call();
        val json = FastJsons.json(item.orNull());
        String keyW = "expireAfterWrite";
        String expireWrite = option.getSpecs().get(keyW);
        if (isNotBlank(expireWrite)) {
            val duration = Durations.parse(keyW, expireWrite, SECONDS);
            redis.set(redisValueKey, json, "NX", "EX", duration);
            onRedisValueStored(redisValueKey, cacheKey, json, duration);
            log.debug("set redis value {}={} with {} seconds expire",
                    redisValueKey, json, duration);
        } else {
            redis.set(redisValueKey, json);
            onRedisValueStored(redisValueKey, cacheKey, json, -1);
            log.debug("set redis value {}={}", redisValueKey, json);
        }

        return item;
    }

    protected void onRedisValueStored(String redisValueKey,
                                      String cacheKey,
                                      String json,
                                      long duration) {
        // maybe used to synchronized with other center.
    }
}
