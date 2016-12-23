package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.WestCacheOptions;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public interface WestCacheKeyStrategy {
    String getCacheKey(WestCacheOptions option, String methodName, Object bean, Object... args);
}
