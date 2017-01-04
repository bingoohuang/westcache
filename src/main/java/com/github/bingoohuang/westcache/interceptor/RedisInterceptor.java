package com.github.bingoohuang.westcache.interceptor;

import com.github.bingoohuang.westcache.base.WestCacheInterceptor;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.Durations;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Redis;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import redis.clients.jedis.JedisCommands;

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
    String prefix = "westcache:";

    @Override
    public WestCacheItem intercept(
            final WestCacheOption option,
            String cacheKey,
            Callable<WestCacheItem> callable) throws Exception {

        final val lockKey = prefix + "lock:" + cacheKey;
        log.debug("wait redis lock {}", lockKey);
        val redis = Redis.getRedis(option);
        waitRedisLock(redis, lockKey);
        log.debug("got redis lock {}", lockKey);
        @Cleanup val i = new Closeable() {
            @Override public void close() throws IOException {
                redis.del(lockKey);
                log.debug("del redis lock {}", lockKey);
            }
        }; // free lock automatically

        val redisValueKey = prefix + cacheKey;
        val redisValue = redis.get(redisValueKey);
        if (redisValue != null) {
            log.debug("got redis value {}", redisValue);
            Object value = FastJsons.parse(redisValue);
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

    @SneakyThrows
    private void waitRedisLock(JedisCommands redis, String lockKey) {
        int maxWaitTimes = 10;
        while (maxWaitTimes-- > 0) {
            Long lock = redis.setnx(lockKey, "lock");
            if (lock == 1L) return;

            Thread.sleep(100L);
        }
    }
}
