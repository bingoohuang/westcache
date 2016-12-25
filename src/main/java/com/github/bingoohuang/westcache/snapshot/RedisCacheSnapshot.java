package com.github.bingoohuang.westcache.snapshot;

import com.alibaba.fastjson.JSON;
import com.github.bingoohuang.westcache.base.WestCacheSnapshot;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import lombok.val;
import redis.clients.jedis.JedisCommands;

import static com.alibaba.fastjson.serializer.SerializerFeature.WriteClassName;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class RedisCacheSnapshot implements WestCacheSnapshot {
    JedisCommands jedisCommands;
    String prefix;

    public RedisCacheSnapshot(JedisCommands jedisCommands, String prefix) {
        this.jedisCommands = jedisCommands;
        this.prefix = prefix;
    }

    public RedisCacheSnapshot(JedisCommands jedisCommands) {
        this(jedisCommands, "westcache:");
    }

    @Override public void saveSnapshot(WestCacheOption option, String cacheKey, Object cacheValue) {
        val json = JSON.toJSONString(cacheValue, WriteClassName);
        jedisCommands.set(prefix + cacheKey, json);
    }

    @Override public <T> Optional<T> readSnapshot(WestCacheOption option, String cacheKey) {
        String json = jedisCommands.get(prefix + cacheKey);
        if (json == null) return null;

        Object object = JSON.parse(json);
        return (Optional<T>) Optional.of(object);
    }

    @Override public void deleteSnapshot(String cacheKey) {
        jedisCommands.del(prefix + cacheKey);
    }
}
