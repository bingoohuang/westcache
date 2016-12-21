package com.github.bingoohuang.westcache.impl;

import com.github.bingoohuang.westcache.WestCachable;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/21.
 */
public class WestCacheMethodInterceptor implements MethodInterceptor {
    private Object target;

    public WestCacheMethodInterceptor() {
    }

    public WestCacheMethodInterceptor(Object target) {
        this.target = target;
    }

    static Cache<String, Optional<Object>> cache = CacheBuilder.newBuilder().build();

    @Override
    public Object intercept(final Object obj,
                            final Method method,
                            final Object[] args,
                            final MethodProxy methodProxy) throws Throwable {
        return isWestCachable(method)
                ? cacheGet(obj, method, args, methodProxy)
                : invokeOriginal(obj, args, methodProxy);
    }

    @SneakyThrows
    private Object invokeOriginal(final Object obj,
                                  final Object[] args,
                                  final MethodProxy methodProxy) {
        return target != null
                ? methodProxy.invoke(target, args)
                : methodProxy.invokeSuper(obj, args);
    }

    @SneakyThrows
    private Object cacheGet(final Object obj,
                            final Method method,
                            final Object[] args,
                            final MethodProxy proxy) {
        String cacheKey = createCacheKey(obj, method, args);

        return cache.get(cacheKey, new Callable<Optional<Object>>() {
            @SneakyThrows @Override
            public Optional<Object> call() throws Exception {
                Object realReturn = invokeOriginal(obj, args, proxy);
                return Optional.fromNullable(realReturn);
            }
        }).orNull();
    }

    private String createCacheKey(final Object obj,
                                  final Method method,
                                  final Object[] args) {
        String className = method.getDeclaringClass().getName();
        return className + "." + method.getName() + "."
                + (target != null ? target.hashCode() : obj.hashCode());
    }

    private boolean isWestCachable(Method method) {
        return method.isAnnotationPresent(WestCachable.class);
    }
}
