package com.github.bingoohuang.westcache.springann;

import com.github.bingoohuang.westcache.spring.WestCacheableEnabled;
import com.github.bingoohuang.westcache.spring.WestCacheableScan;
import com.github.bingoohuang.westcache.utils.EmbeddedRedis;
import com.github.bingoohuang.westcache.utils.Redis;
import lombok.val;
import org.n3r.eql.eqler.spring.EqlerScan;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.JedisCommands;

@Configuration @ComponentScan
@EqlerScan
@WestCacheableScan(value = "com.github.bingoohuang.westcache.springann",
        basePackageClasses = SpringConfig.class)
@WestCacheableEnabled
public class SpringConfig {
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        val creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    @Bean @Primary
    public JedisCommands thisJedisCommands() {
        return Redis.createJedisCommands(
                "127.0.0.1", EmbeddedRedis.port1, 10);
    }

    @Bean(name = "that")
    public JedisCommands thatJedisCommands() {
        return Redis.createJedisCommands(
                "127.0.0.1", EmbeddedRedis.port2, 10);
    }

    @Bean(name = "cacheFlushScheduledBean")
    public String cacheFlushScheduled() {
        return "Every 1 seconds";
    }
}
