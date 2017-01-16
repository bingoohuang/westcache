package com.github.bingoohuang.westcache.flusher;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheFlusher;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public class ByPassCacheFlusher implements WestCacheFlusher {
    @Override
    public boolean isKeyEnabled(WestCacheOption option, String cacheKey) {
        return true;
    }

    @Override
    public Optional<Object> getDirectValue(WestCacheOption option, String cacheKey) {
        return Optional.absent();
    }

    @Override
    public boolean register(WestCacheOption option, String cacheKey, WestCache cache) {
        return false;
    }

    @Override public boolean flush(WestCacheOption option, String cacheKey, String version) {
        return false;
    }
}
