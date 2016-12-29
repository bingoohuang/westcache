package com.github.bingoohuang.westcache.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
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
        public WestCacheItem get(WestCacheOption option, String cacheKey, Callable<WestCacheItem> callable) {
            String jsonValue = jedis.get(prefix + cacheKey);
            if (StringUtils.isEmpty(jsonValue))
                return new WestCacheItem(null);

            Object object = JSON.parse(jsonValue);
            return new WestCacheItem(object);
        }

        @Override
        public WestCacheItem getIfPresent(WestCacheOption option, String cacheKey) {
            return get(option, cacheKey, null);
        }

        @Override
        public void put(WestCacheOption option, String cacheKey, WestCacheItem cacheValue) {
            if (cacheValue != null) {
                val json = JSON.toJSONString(cacheValue.getObject().get(), SerializerFeature.WriteClassName);
                jedis.set(prefix + cacheKey, json);
            } else {
                jedis.del(prefix + cacheKey);
            }

        }

        @Override
        public void invalidate(WestCacheOption option, String cacheKey) {
            jedis.del(prefix + cacheKey);
        }
    }
}
