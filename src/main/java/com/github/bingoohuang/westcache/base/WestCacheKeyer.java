package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.utils.WestCacheOption;

import java.lang.reflect.Method;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public abstract class WestCacheKeyer {
    public abstract String getCacheKey(WestCacheOption option,
                                       String methodName,
                                       Object bean,
                                       Object... args);

    public String getCacheKey(WestCacheOption option,
                              Method method,
                              Object bean,
                              Object... args) {
        return getCacheKey(option, method.getName(), bean, args);
    }
}
