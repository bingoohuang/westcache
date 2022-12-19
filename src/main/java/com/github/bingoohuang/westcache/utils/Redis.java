package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.utils.lang.Threadx;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.spring.SpringAppContext;
import com.google.common.base.Optional;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/3.
 */
@Slf4j @UtilityClass
public class Redis {
    public static final String PREFIX = "westcache:";
    private static JedisCommands jedis = createJedisCommands("127.0.0.1", 6379, 10);

    public static void setJedis(JedisCommands settedJedis) {
        jedis = settedJedis;
    }

    public static JedisCommands getJedis() {
        return jedis;
    }

    public static JedisCommands getRedis(WestCacheOption option) {
        String redisBean = option.getSpecs().get("redisBean");

        if (Envs.HAS_SPRING && StringUtils.isNotEmpty(redisBean)) {
            JedisCommands bean = SpringAppContext.getBeanOrNull(redisBean);
            if (bean != null) return bean;
        }

        if (Envs.HAS_SPRING) {
            val bean = SpringAppContext.getBeanOrNull(JedisCommands.class);
            if (bean != null) return bean;
        }

        return jedis;
    }

    public static JedisCommands createJedisCommands(
            String host, int port, int maxTotal) {
        val poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);

        val pool = new JedisPool(poolConfig, host, port);
        return (Jedis) Cglibs.proxy(Jedis.class, (o1, m1, args1, p1) -> {
            val pooled = pool.getResource();
            val result = m1.invoke(pooled, args1);
            switch (m1.getName()) {
                case "multi":
                    return Cglibs.proxy(Transaction.class, (o2, m2, args2, p2) -> {
                        Object o = m2.invoke(result, args2);
                        if (m2.getName().equals("exec")) pooled.close();
                        return o;
                    });
                case "close":
                    pool.destroy();
                    break;
                default:
                    pooled.close();
            }
            return result;
        });
    }


    public static boolean waitRedisLock(JedisCommands redis, String lockKey) {
        int maxWaitTimes = 10;
        while (maxWaitTimes-- > 0) {
            Long lock = redis.setnx(lockKey, "lock");
            if (lock == 1L) return true;

            Threadx.sleepMillis(50L);
        }
        return false;
    }

    public static WestCacheItem getWestCacheItem(WestCacheOption option,
                                                 JedisCommands redis,
                                                 String redisKey) {
        val jsonValue = redis.get(redisKey);
        if (jsonValue == null) return null;

        val value = FastJsons.parse(jsonValue, option.getMethod(), true);
        if (value == null && !"null".equals(jsonValue)) return null;

        val optional = Optional.fromNullable(value);
        return new WestCacheItem(optional, option);
    }

    public static String expirePut(WestCacheOption option,
                                   JedisCommands redis,
                                   String redisKey,
                                   WestCacheItem item) {
        val duration = item.getDurationSeconds();

        val json = FastJsons.json(item.orNull(), option.getMethod());
        val result = redis.set(redisKey, json);
        if (duration > 0) {
            log.info("redis set {}={} in ttl {} seconds", redisKey, json, duration);
            redis.expire(redisKey, (int) duration);
        }

        return result;
    }
}
