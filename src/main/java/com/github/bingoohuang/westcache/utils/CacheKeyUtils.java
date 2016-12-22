package com.github.bingoohuang.westcache.utils;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@UtilityClass
public class CacheKeyUtils {
    public String createCacheKey(Class<?> clazz, String methodName) {
        return clazz.getName() + "." + methodName;
    }

    public String createCacheKey(Method method) {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    public String createSnapshotCacheKey(Object obj, String methodName) {
        return CglibUtils.getSuperClassName(obj) + "." + methodName;
    }

    public String createCacheKey(Object obj, String methodName) {
        return CglibUtils.getSuperClassName(obj) + "." + methodName + "." + obj.hashCode();
    }

    public String createCacheKey(Object obj, Method method, Object[] args) {
        return method.getDeclaringClass().getName() + "." + method.getName() + "." + obj.hashCode();
    }
}
