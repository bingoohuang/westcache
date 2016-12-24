package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.cglib.CacheMethodInterceptor;
import com.github.bingoohuang.westcache.cglib.Cglibs;
import com.github.bingoohuang.westcache.cglib.WestCacheCglib;
import lombok.SneakyThrows;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/21.
 */
public class WestCacheFactory {
    @SneakyThrows
    public static <T> T create(Class<T> targetClass) {
        if (WestCacheCglib.class.isAssignableFrom(targetClass)) {
            return targetClass.newInstance();
        }

        return (T) Cglibs.proxy(targetClass,
                new CacheMethodInterceptor(), WestCacheCglib.class);
    }

    public static <T> T create(T target) {
        if (target instanceof WestCacheCglib) return target;

        return (T) Cglibs.proxy(target.getClass(),
                new CacheMethodInterceptor(target), WestCacheCglib.class);
    }
}
