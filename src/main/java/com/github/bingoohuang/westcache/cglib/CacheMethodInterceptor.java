package com.github.bingoohuang.westcache.cglib;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
@Slf4j
public abstract class CacheMethodInterceptor<T> {
    protected abstract Object invokeRaw(Object obj,
                                        Object[] args,
                                        T methodProxy);

    protected abstract String getCacheKey(WestCacheOption option,
                                          Object obj,
                                          Method method,
                                          Object[] args,
                                          T proxy);

    public Object intercept(Object obj,
                            Method method,
                            Object[] args,
                            T methodProxy) {
        val option = WestCacheOption.parseWestCacheable(method);
        if (option == null) return invokeRaw(obj, args, methodProxy);

        return cacheGet(option, obj, method, args, methodProxy);
    }


    @SneakyThrows
    private Object cacheGet(final WestCacheOption option,
                            final Object obj,
                            final Method method,
                            final Object[] args,
                            final T proxy) {
        val cacheKey = getCacheKey(option, obj, method, args, proxy);

        val start = System.currentTimeMillis();
        @Cleanup val i = new Closeable() {
            @Override public void close() {
                val end = System.currentTimeMillis();
                log.debug("cost {} millis to get cache {} ", (end - start), cacheKey);
            }
        };

        val item = option.getManager().get(option, cacheKey,
                new Callable<WestCacheItem>() {
                    @Override public WestCacheItem call() {
                        checkNoneAbstractMethod(cacheKey, method);
                        Object raw = invokeRaw(obj, args, proxy);
                        return new WestCacheItem(raw);
                    }
                });
        return item.getObject().orNull();
    }

    private void checkNoneAbstractMethod(String cacheKey, Method method) {
        if (!Modifier.isAbstract(method.getModifiers())) return;

        val msg = "cache key " + cacheKey + " missed executable body in abstract method "
                + method.getDeclaringClass().getName() + "." + method.getName();
        throw new RuntimeException(msg);
    }
}
