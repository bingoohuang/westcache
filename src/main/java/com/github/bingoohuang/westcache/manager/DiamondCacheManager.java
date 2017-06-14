package com.github.bingoohuang.westcache.manager;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import lombok.val;
import org.n3r.diamond.client.Miner;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
public class DiamondCacheManager extends BaseCacheManager {
    public static final String GROUP = "west.cache.manager";

    public DiamondCacheManager() {
        super(new DiamondWestCache());
    }

    public static class DiamondWestCache implements WestCache {
        @Override
        public WestCacheItem get(WestCacheOption option,
                                 String cacheKey,
                                 Callable<WestCacheItem> callable) {
            val json = new Miner().getStone(GROUP, cacheKey);
            val object = FastJsons.parse(json, option.getMethod(), true);
            val optional = Optional.fromNullable(object);
            return new WestCacheItem(optional, option);
        }

        @Override
        public WestCacheItem getIfPresent(WestCacheOption option,
                                          String cacheKey) {
            return get(option, cacheKey, null);
        }

        @Override
        public void put(WestCacheOption option,
                        String cacheKey,
                        WestCacheItem cacheValue) {
            throw new UnsupportedOperationException(
                    "DiamondCacheManager put is unsupported");
        }

        @Override
        public void invalidate(WestCacheOption option, String cacheKey, String version) {
            throw new UnsupportedOperationException(
                    "DiamondCacheManager invalidate is unsupported");
        }
    }
}
