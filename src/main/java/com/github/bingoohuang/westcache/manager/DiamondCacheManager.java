package com.github.bingoohuang.westcache.manager;

import com.alibaba.fastjson.JSON;
import com.github.bingoohuang.westcache.base.WestCache;
import com.google.common.base.Optional;
import org.n3r.diamond.client.Miner;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
public class DiamondCacheManager extends BaseCacheManager {
    public static String GROUP = "west.cache.manager";
    DiamondWestCache westCache = new DiamondWestCache();

    @Override public WestCache<String, Object> getWestCache() {
        return westCache;
    }

    public static class DiamondWestCache implements WestCache<String, Object> {
        @Override public Object get(String cacheKey, Callable<?> callable) {
            String json = new Miner().getStone(GROUP, cacheKey);
            return Optional.fromNullable(JSON.parse(json));
        }

        @Override public Object getIfPresent(String cacheKey) {
            return get(cacheKey, null);
        }

        @Override public void put(String cacheKey, Object cacheValue) {
            throw new UnsupportedOperationException("DiamondCacheManager put is unsupported");
        }

        @Override public void invalidate(String cacheKey) {
            throw new UnsupportedOperationException("DiamondCacheManager put is unsupported");
        }
    }
}
