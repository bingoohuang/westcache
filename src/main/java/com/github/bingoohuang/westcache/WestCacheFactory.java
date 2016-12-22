package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.impl.CacheMethodInterceptor;
import lombok.val;
import net.sf.cglib.proxy.Enhancer;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/21.
 */
public class WestCacheFactory {
    public static <T> T create(Class<T> targetClass) {
        val interceptor = new CacheMethodInterceptor();
        return (T) Enhancer.create(targetClass, interceptor);
    }

    public static <T> T create(T target) {
        val interceptor = new CacheMethodInterceptor(target);
        return (T) Enhancer.create(target.getClass(), interceptor);
    }
}
