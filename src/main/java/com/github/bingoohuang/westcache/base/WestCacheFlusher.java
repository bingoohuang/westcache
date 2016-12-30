package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCacheFlusher {
    boolean isKeyEnabled(WestCacheOption option, String cacheKey);

    boolean register(WestCacheOption option,
                     String cacheKey,
                     WestCache cache);

    boolean flush(WestCacheOption option, String cacheKey);

    Optional<Object> getDirectValue(WestCacheOption option, String cacheKey);
}
