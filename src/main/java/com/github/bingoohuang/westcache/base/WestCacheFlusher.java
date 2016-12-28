package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.utils.WestCacheOption;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCacheFlusher {
    boolean isKeyEnabled(WestCacheOption option, String cacheKey);

    boolean register(WestCacheOption option,
                     String cacheKey,
                     WestCache<String, Object> cache);

    boolean flush(String cacheKey);

    <T> T getDirectValue(WestCacheOption option, String cacheKey);
}
