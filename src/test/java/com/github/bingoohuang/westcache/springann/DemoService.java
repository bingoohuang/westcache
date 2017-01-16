package com.github.bingoohuang.westcache.springann;

import com.github.bingoohuang.westcache.WestCacheable;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCommands;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
@Service public class DemoService {
    @Setter String data;

    @WestCacheable
    public String cachedMethod() {
        return data;
    }

    @WestCacheable(manager = "redis", keyer = "simple",
            specs = "redisBean=that")
    public String doSth() {
        return System.currentTimeMillis() + "";
    }

    @Autowired JedisCommands thisRedis;
    @Autowired @Qualifier("that") JedisCommands thatRedis;

    public void setXxx() {
        thisRedis.set("xxx", "bingoo");
        thatRedis.set("yyy", "huang");
    }

    @WestCacheable(
            manager = "quartz-manager",
            flusher = "quartz-flusher",
            specs = "scheduledBean=cacheFlushScheduledBean")
    public long doWhat() {
        return System.currentTimeMillis();
    }
}
