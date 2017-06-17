package com.github.bingoohuang.westcache.peng;

import com.github.bingoohuang.westcache.spring.WestCacheableEnabled;
import com.github.bingoohuang.westcache.utils.EmbeddedRedis;
import com.github.bingoohuang.westcache.utils.Redis;
import lombok.val;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisCommands;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/7.
 */
@Configuration @ComponentScan @WestCacheableEnabled
public class PengConfig {
    @Bean(name = "singleRedis")
    public JedisCommands singleRedis() {
        return Redis.createJedisCommands(
                "127.0.0.1", EmbeddedRedis.port1, 10);
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        val creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }
}
