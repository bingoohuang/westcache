package com.github.bingoohuang.westcache.utils;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/11.
 */
@Slf4j @UtilityClass
public class Envs {
    public static final boolean HAS_SPRING = classExists("org.springframework.context.ApplicationContext");
    public static final boolean HAS_DIAMOND = classExists("org.n3r.diamond.client.DiamondListener");
    public static final boolean HAS_EXPIRING = classExists("net.jodah.expiringmap.ExpiringMap");
    public static final boolean HAS_JEDIS = classExists("redis.clients.jedis.JedisCommands");
    public static final boolean HAS_EQL = classExists("org.n3r.eql.eqler.EqlerFactory");
    public static final boolean HAS_QUARTZ = classExists("org.quartz.JobDetail");

    public static boolean classExists(String className) {
        try {
            Class.forName(className, false, Envs.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @SneakyThrows
    public static <T> T execute(Callable<T> callable) {
        return callable.call();
    }

    @SneakyThrows
    public static void sleepMillis(long millis) {
        Thread.sleep(millis);
    }

    @SneakyThrows
    public static <T> T futureGet(Future<T> future, long timeoutInMillis) throws TimeoutException {
        try {
            return future.get(timeoutInMillis, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            log.warn("futureGet error", e);
            throw e.getCause();
        }
    }

    @SneakyThrows
    public static <T> T futureGet(Future<T> future) {
        try {
            return future.get();
        } catch (ExecutionException e) {
            log.warn("futureGet error", e);
            throw e.getCause();
        }
    }

    @SneakyThrows
    public static Class forName(String className) {
        return Class.forName(className);
    }

    @SneakyThrows @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className) {
        return (T) forName(className).newInstance();
    }

    @SneakyThrows
    public static <T> T invoke(Method m, Object object) {
        try {
            return (T) m.invoke(object);
        } catch (InvocationTargetException e) {
            log.warn("invoke method {} error", m, e);
            throw e.getCause();
        }
    }

    public static <T> T trySnapshot(WestCacheOption option,
                                    Future<T> future,
                                    String cacheKey) {
        val timeoutMillis = option.getConfig().timeoutMillisToSnapshot();
        try {
            return futureGet(future, timeoutMillis);
        } catch (TimeoutException ex) {
            log.info("get cache {} timeout in {} millis," +
                    " try snapshot", cacheKey, timeoutMillis);
            val result = option.getSnapshot().readSnapshot(option, cacheKey);
            log.info("got {} snapshot {}", cacheKey,
                    result != null ? result.getObject() : " non-exist");
            return result != null ? (T) result : Envs.futureGet(future);
        }
    }

    @SneakyThrows
    public static List<String> loadClasspathResources(String name, ClassLoader classLoader) {
        ArrayList<String> list = new ArrayList();
        val loader = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
        val systemResources = loader.getResources(name);
        while (systemResources.hasMoreElements()) {
            @Cleanup val stream = systemResources.nextElement().openStream();
            val result = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
            list.add(result);
        }

        return list;
    }
}
