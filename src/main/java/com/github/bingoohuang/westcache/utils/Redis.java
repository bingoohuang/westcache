package com.github.bingoohuang.westcache.utils;

import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.Closeable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/3.
 */
public class Redis {
    public static JedisCommands jedis = createtJedisCommands(
            "127.0.0.1", 6379, 10);

    public static JedisCommands createtJedisCommands(
            String host, int port, int maxTotal) {
        val poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);

        val pool = new JedisPool(poolConfig, host, port);
        return proxyJedisCommands(pool);
    }

    private static JedisCommands proxyJedisCommands(JedisPool pool) {
        return (JedisCommands) Proxy.newProxyInstance(
                JedisInvocationHandler.class.getClassLoader(),
                new Class[]{JedisCommands.class},
                new JedisInvocationHandler(pool));
    }

    @AllArgsConstructor
    public static class JedisInvocationHandler implements InvocationHandler {
        final JedisPool pool;

        @Override
        public Object invoke(Object proxy,
                             Method method,
                             Object[] args) throws Throwable {
            val jedis = pool.getResource();
            @Cleanup val i = new Closeable() {
                @Override @SneakyThrows public void close() {
                    jedis.close();
                }
            };

            return method.invoke(jedis, args);
        }
    }
}
