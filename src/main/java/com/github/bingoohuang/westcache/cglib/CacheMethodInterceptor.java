package com.github.bingoohuang.westcache.cglib;

import com.github.bingoohuang.westcache.base.WestCacheException;
import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.QuietCloseable;
import com.github.bingoohuang.westcache.utils.WestCacheConnector;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
@Slf4j
public abstract class CacheMethodInterceptor<T> {
    protected abstract Object invokeRaw(Object obj, Object[] args, T methodProxy);

    protected abstract String getCacheKey(WestCacheOption option, Object obj, Method method, Object[] args, T proxy);

    @SneakyThrows
    public Object intercept(Object obj, Method method, Object[] args, T methodProxy) {
        val option = WestCacheOption.parseWestCacheable(method);
        if (option == null)
            return invokeRaw(obj, args, methodProxy);

        try {
            return cacheGet(option, obj, method, args, methodProxy);
        } catch (Exception ex) {
            log.error("cache get error", ex);
            throw ex;
        }
    }

    private Object cacheGet(final WestCacheOption option,
                            final Object obj,
                            final Method method,
                            final Object[] args,
                            final T proxy) {
        val cacheKey = getCacheKey(option, obj, method, args, proxy);
        if (WestCacheConnector.isConnectedAndGoon(option, cacheKey))
            return null;

        val start = System.currentTimeMillis();
        @Cleanup QuietCloseable i = () -> {
            val end = System.currentTimeMillis();
            log.debug("cost {} millis to get cache {} ", end - start, cacheKey);
        };

        val item = option.getManager().get(option, cacheKey,
                () -> {
                    checkNoneAbstractMethod(cacheKey, method);
                    val raw = invokeRaw(obj, args, proxy);
                    val optional = Optional.fromNullable(raw);
                    return new WestCacheItem(optional, option);
                });
        return item.getObject().orNull();
    }

    private void checkNoneAbstractMethod(String cacheKey, Method method) {
        if (!Modifier.isAbstract(method.getModifiers())) return;

        val msg = "cache key " + cacheKey + " missed executable body "
                + "in abstract method " + method.getDeclaringClass().getName()
                + "." + method.getName();
        throw new WestCacheException(msg);
    }
}
