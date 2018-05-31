package com.github.bingoohuang.westcache.interceptor;

import com.github.bingoohuang.westcache.base.WestCacheInterceptor;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.Envs;
import com.github.bingoohuang.westcache.utils.QuietCloseable;
import com.github.bingoohuang.westcache.utils.Redis;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import redis.clients.jedis.JedisCommands;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/3.
 */
@Slf4j
public class RedisInterceptor implements WestCacheInterceptor {
    @Override
    public WestCacheItem intercept(final WestCacheOption option,
                                   String cacheKey,
                                   Callable<WestCacheItem> callable) {
        val redisKey = Redis.PREFIX + cacheKey;
        val redis = Redis.getRedis(option);
        val item0 = Redis.getWestCacheItem(option, redis, redisKey);
        if (item0 != null) return item0;

        if (!"true".equals(option.getSpecs().get("redisLockFirst"))) {
            return executeAndPut(option, callable, redisKey, redis);
        }

        val lockKey = Redis.PREFIX + "lock:" + cacheKey;
        val locked = Redis.waitRedisLock(redis, lockKey);
        log.debug("got redis lock {}={}", lockKey, locked);

        @Cleanup QuietCloseable i = () -> {
            redis.del(lockKey);
            log.debug("del redis lock {}", lockKey);
        }; // free lock automatically

        val item1 = Redis.getWestCacheItem(option, redis, redisKey);
        if (item1 != null) return item1;

        return executeAndPut(option, callable, redisKey, redis);
    }

    private WestCacheItem executeAndPut(WestCacheOption option,
                                        Callable<WestCacheItem> callable,
                                        String redisKey, JedisCommands redis) {
        val item = Envs.execute(callable);
        Redis.expirePut(option, redis, redisKey, item);

        return item;
    }
}
