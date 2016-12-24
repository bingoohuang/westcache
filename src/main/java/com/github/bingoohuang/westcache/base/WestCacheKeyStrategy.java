package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.WestCacheOptions;

import java.lang.reflect.Method;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public abstract class WestCacheKeyStrategy {
    public abstract String getCacheKey(WestCacheOptions option,
                                       String methodName,
                                       Object bean,
                                       Object... args);

    public String getCacheKey(WestCacheOptions option,
                              Method method,
                              Object bean,
                              Object... args) {
        return getCacheKey(option, method.getName(), bean, args);
    }
}
