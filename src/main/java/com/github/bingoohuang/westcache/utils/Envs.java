package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.utils.lang.Clz;
import com.github.bingoohuang.utils.lang.Futures;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/11.
 */
@Slf4j @UtilityClass
public class Envs {
    public static final boolean HAS_SPRING = Clz.classExists("org.springframework.context.ApplicationContext");
    public static final boolean HAS_DIAMOND = Clz.classExists("org.n3r.diamond.client.DiamondListener");
    public static final boolean HAS_EXPIRING = Clz.classExists("net.jodah.expiringmap.ExpiringMap");
    public static final boolean HAS_JEDIS = Clz.classExists("redis.clients.jedis.JedisCommands");
    public static final boolean HAS_EQL = Clz.classExists("org.n3r.eql.eqler.EqlerFactory");
    public static final boolean HAS_QUARTZ = Clz.classExists("org.quartz.JobDetail");


    public static <T> T trySnapshot(WestCacheOption option,
                                    Future<T> future,
                                    String cacheKey) {
        val timeoutMillis = option.getConfig().timeoutMillisToSnapshot();
        try {
            return Futures.futureGet(future, timeoutMillis);
        } catch (TimeoutException ex) {
            log.info("get cache {} timeout in {} millis," +
                    " try snapshot", cacheKey, timeoutMillis);
            val result = option.getSnapshot().readSnapshot(option, cacheKey);
            log.info("got {} snapshot {}", cacheKey,
                    result != null ? result.getObject() : " non-exist");
            return result != null ? (T) result : Futures.futureGet(future);
        }
    }

}
