package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.utils.WestCacheOption;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/3.
 */
public interface WestCacheInterceptor {
    WestCacheItem intercept(
            WestCacheOption option,
            String cacheKey,
            Callable<WestCacheItem> callable);
}
