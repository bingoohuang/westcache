package com.github.bingoohuang.westcache.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.bingoohuang.westcache.base.WestCache;
import com.google.common.base.Optional;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisCommands;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public class RedisCacheManager extends BaseCacheManager {
    public RedisCacheManager(JedisCommands jedis, String prefix) {
        super(new RedisWestCache(jedis, prefix));
    }

    public RedisCacheManager(JedisCommands jedis) {
        super(new RedisWestCache(jedis));
    }

    private static class RedisWestCache implements WestCache {
        JedisCommands jedis;
        String prefix;

        public RedisWestCache(JedisCommands jedis, String prefix) {
            this.jedis = jedis;
            this.prefix = prefix;
        }

        public RedisWestCache(JedisCommands jedis) {
            this(jedis, "westcache:");
        }


        @Override @SneakyThrows
        public Optional<Object> get(String cacheKey, Callable<Optional<Object>> callable) {
            String jsonValue = jedis.get(prefix + cacheKey);
            if (StringUtils.isEmpty(jsonValue)) return Optional.absent();

            Object object = JSON.parse(jsonValue);
            return Optional.fromNullable(object);
        }

        @Override public Optional<Object> getIfPresent(String cacheKey) {
            return get(cacheKey, null);
        }

        @Override
        public void put(String cacheKey, Optional<Object> cacheValue) {
            if (cacheValue.isPresent()) {
                val json = JSON.toJSONString(cacheValue.get(), SerializerFeature.WriteClassName);
                jedis.set(prefix + cacheKey, json);
            } else {
                jedis.del(prefix + cacheKey);
            }

        }

        @Override public void invalidate(String cacheKey) {
            jedis.del(prefix + cacheKey);
        }
    }
}
