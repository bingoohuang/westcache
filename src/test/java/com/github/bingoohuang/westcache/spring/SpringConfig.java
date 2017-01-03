package com.github.bingoohuang.westcache.spring;

import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.Closeable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Configuration @ComponentScan @WestCacheableScan
public class SpringConfig {
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        val creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    @Bean(name = "this")
    public JedisCommands thisJedisCommands() {
        val jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(10);

        val pool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379);
        return proxyJedisCommands(pool);
    }

    @Bean(name = "that")
    public JedisCommands thatJedisCommands() {
        val jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(10);

        val pool = new JedisPool(jedisPoolConfig, "127.0.0.1", 7379);
        return proxyJedisCommands(pool);
    }

    private JedisCommands proxyJedisCommands(JedisPool pool) {
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
