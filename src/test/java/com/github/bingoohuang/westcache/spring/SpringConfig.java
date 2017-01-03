package com.github.bingoohuang.westcache.spring;

import com.github.bingoohuang.westcache.utils.Redis;
import lombok.val;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisCommands;

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
        return Redis.createtJedisCommands(
                "127.0.0.1", 6379, 10);
    }

    @Bean(name = "that")
    public JedisCommands thatJedisCommands() {
        return Redis.createtJedisCommands(
                "127.0.0.1", 7379, 10);
    }

}
