package com.github.bingoohuang.westcache.manager;

import com.alibaba.fastjson.JSON;
import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import org.n3r.diamond.client.Miner;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
public class DiamondCacheManager extends BaseCacheManager {
    public static String GROUP = "west.cache.manager";

    public DiamondCacheManager() {
        super(new DiamondWestCache());
    }

    public static class DiamondWestCache implements WestCache {
        @Override
        public Optional<Object> get(WestCacheOption option, String cacheKey, Callable<Optional<Object>> callable) {
            String json = new Miner().getStone(GROUP, cacheKey);
            return Optional.fromNullable(JSON.parse(json));
        }

        @Override public Optional<Object> getIfPresent(WestCacheOption option, String cacheKey) {
            return get(option, cacheKey, null);
        }

        @Override
        public void put(WestCacheOption option, String cacheKey, Optional<Object> cacheValue) {
            throw new UnsupportedOperationException("DiamondCacheManager put is unsupported");
        }

        @Override public void invalidate(WestCacheOption option, String cacheKey) {
            throw new UnsupportedOperationException("DiamondCacheManager put is unsupported");
        }
    }
}
