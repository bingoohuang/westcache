package com.github.bingoohuang.westcache.flusher;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheFlusher;
import com.github.bingoohuang.westcache.utils.WestCacheOption;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public class NoneCacheFlusher implements WestCacheFlusher {
    @Override
    public boolean register(WestCacheOption option, String cacheKey, WestCache cache) {
        return false;
    }

    @Override public boolean flush(String cacheKey) {
        return false;
    }

    @Override public <T> T getDirectValue(WestCacheOption option, String cacheKey) {
        return null;
    }

    @Override
    public boolean isKeyEnabled(WestCacheOption option, String cacheKey) {
        return true;
    }
}
