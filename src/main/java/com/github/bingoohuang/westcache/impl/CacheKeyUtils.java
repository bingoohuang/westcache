package com.github.bingoohuang.westcache.impl;

import java.lang.reflect.Method;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class CacheKeyUtils {
    public static String createCacheKey(Class<?> clazz, String methodName) {
        String className = clazz.getName();
        return className + "." + methodName;
    }

    public static String createCacheKey(Method method) {
        return createCacheKey(method.getDeclaringClass(), method.getName());
    }

    public static String createCacheKey(Object obj, Method method, Object[] args) {
        return createCacheKey(method) + "." + obj.hashCode();
    }
}
