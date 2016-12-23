package com.github.bingoohuang.westcache.flusher;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.base.WestCacheFlusher;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public class NoneCacheFlusher implements WestCacheFlusher {
    @Override
    public void register(String cacheKey, WestCache<String, Object> cache) {

    }

    @Override public void flush(String cacheKey) {

    }
}
