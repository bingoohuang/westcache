package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.impl.WestCacheMethodInterceptor;
import lombok.val;
import net.sf.cglib.proxy.Enhancer;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/21.
 */
public class WestCacheFactory {
    public static <T> T wrap(Class<T> targetClass) {
        val interceptor = new WestCacheMethodInterceptor();
        return (T) Enhancer.create(targetClass, interceptor);
    }

    public static <T> T wrap(T target) {
        val interceptor = new WestCacheMethodInterceptor(target);
        return (T) Enhancer.create(target.getClass(), interceptor);
    }
}
