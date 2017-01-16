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

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/3.
 */
@Slf4j
public class RedisInterceptor implements WestCacheInterceptor {
    @Override
    public WestCacheItem intercept(
            final WestCacheOption option,
            String cacheKey,
            Callable<WestCacheItem> callable) {
        val redisKey = Redis.PREFIX + cacheKey;
        val redis = Redis.getRedis(option);
        val item1 = Redis.getWestCacheItem(option, redis, redisKey);
        if (item1 != null) return item1;

        val lockKey = Redis.PREFIX + "lock:" + cacheKey;
        Redis.waitRedisLock(redis, lockKey);
        log.debug("got redis lock {}", lockKey);

        @Cleanup val i = new QuietCloseable() {
            @Override public void close() {
                redis.del(lockKey);
                log.debug("del redis lock {}", lockKey);
            }
        }; // free lock automatically

        val item2 = Redis.getWestCacheItem(option, redis, redisKey);
        if (item2 != null) return item2;

        val item = Envs.execute(callable);
        Redis.expirePut(option, redis, redisKey, item);

        return item;
    }
}
