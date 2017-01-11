package com.github.bingoohuang.westcache.utils;

import lombok.SneakyThrows;
import lombok.val;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/11.
 */
public abstract class Envs {
    public static boolean hasSpring = classExists("org.springframework.context.ApplicationContext");
    public static boolean hasDiamond = classExists("org.n3r.diamond.client.DiamondListener");
    public static boolean hasExpiring = classExists("net.jodah.expiringmap.ExpiringMap");
    public static boolean hasJedis = classExists("redis.clients.jedis.JedisCommands");
    public static boolean hasEql = classExists("org.n3r.eql.eqler.EqlerFactory");

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
    public static <T> T futureGet(Future<T> future, long timeout) throws TimeoutException {
        return future.get(timeout, TimeUnit.MILLISECONDS);
    }

    @SneakyThrows
    public static <T> T futureGet(Future<T> future) {
        return future.get();
    }

    @SneakyThrows @SuppressWarnings("unchecked")
    public static <T> T newInstance(String loaderClass) {
        val clazz = Class.forName(loaderClass);
        return (T) clazz.newInstance();
    }
}
