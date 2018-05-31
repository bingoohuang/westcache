package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.cglib.Cglibs;
import com.github.bingoohuang.westcache.spring.SpringAppContext;
import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
            JedisCommands bean = SpringAppContext.getBean(redisBean);
            if (bean != null) return bean;
        }

        if (Envs.HAS_SPRING) {
            val bean = SpringAppContext.getBean(JedisCommands.class);
            if (bean != null) return bean;
        }

        return jedis;
    }

    public static JedisCommands createJedisCommands(
            String host, int port, int maxTotal) {
        val poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);

        val pool = new JedisPool(poolConfig, host, port);
        return proxyJedisCommands(pool);
    }

    public static Jedis proxyJedis(final JedisPool pool) {
        return (Jedis) Cglibs.proxy(Jedis.class, (o, method, args, methodProxy) -> {
            @Cleanup val jedis = pool.getResource();
            return method.invoke(jedis, args);
        });
    }

    public static JedisCommands proxyJedisCommands(JedisPool pool) {
        return (JedisCommands) Proxy.newProxyInstance(
                JedisInvocationHandler.class.getClassLoader(),
                new Class[]{JedisCommands.class},
                new JedisInvocationHandler(pool));
    }

    public static boolean waitRedisLock(JedisCommands redis, String lockKey) {
        int maxWaitTimes = 10;
        while (maxWaitTimes-- > 0) {
            Long lock = redis.setnx(lockKey, "lock");
            if (lock == 1L) return true;

            Envs.sleepMillis(50L);
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

    @AllArgsConstructor
    public static class JedisInvocationHandler implements InvocationHandler {
        final JedisPool pool;

        @Override
        public Object invoke(Object proxy,
                             Method method,
                             Object[] args) throws Throwable {
            val jedis = pool.getResource();
            @Cleanup QuietCloseable i = () -> jedis.close();

            return method.invoke(jedis, args);
        }
    }
}
