package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.cglib.CglibCacheMethodInterceptor;
import com.github.bingoohuang.westcache.cglib.Cglibs;
import com.github.bingoohuang.westcache.cglib.WestCacheCglib;
import lombok.SneakyThrows;
import lombok.val;


/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/21.
 */
public abstract class WestCacheFactory {
    private static final CglibCacheMethodInterceptor INTERCEPTOR = new CglibCacheMethodInterceptor();

    /**
     * Create a proxied object of target class.
     * <p>
     * If the target class is already a sub-class of WestCacheCglib, a
     * direct instance object will be created by target class.
     * If the target class is an interface or class, a proxied object
     * created by cglib will be created. And the method annotated with
     *
     * @param targetClass target class
     * @param <T>         target class type
     * @return proxied object.
     */
    @SneakyThrows @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> targetClass) {
        val wccClass = WestCacheCglib.class.isAssignableFrom(targetClass);
        if (wccClass) return targetClass.newInstance();

        return (T) (targetClass.isInterface()
                ? Cglibs.proxy(Object.class, INTERCEPTOR, targetClass, WestCacheCglib.class)
                : Cglibs.proxy(targetClass, INTERCEPTOR, WestCacheCglib.class));

    }

    /**
     * Create a proxied object of target object.
     *
     * @param target target object.
     * @param <T>    target class.
     * @return proxied object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(T target) {
        if (target instanceof WestCacheCglib) return target;

        val interceptor = new CglibCacheMethodInterceptor(target);
        return (T) Cglibs.proxy(target.getClass(), interceptor, WestCacheCglib.class);
    }
}
