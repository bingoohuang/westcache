package com.github.bingoohuang.westcache.interceptor;

import com.github.bingoohuang.westcache.base.WestCacheInterceptor;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.Envs;
import com.github.bingoohuang.westcache.utils.WestCacheOption;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/3.
 */
public class ByPassInterceptor implements WestCacheInterceptor {
    @Override
    public WestCacheItem intercept(
            WestCacheOption option,
            String cacheKey,
            Callable<WestCacheItem> callable) {
        return Envs.execute(callable);
    }
}
