package com.github.bingoohuang.westcache.cglib;

import com.github.bingoohuang.westcache.utils.WestCacheOptions;
import com.github.bingoohuang.westcache.utils.WestCacheAnns;
import com.google.common.base.Optional;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/21.
 */
@Slf4j @NoArgsConstructor @AllArgsConstructor
public class CacheMethodInterceptor implements MethodInterceptor {
    private Object target;

    @Override
    public Object intercept(final Object obj,
                            final Method method,
                            final Object[] args,
                            final MethodProxy methodProxy) {
        val ann = WestCacheAnns.parseWestCacheable(method);
        if (ann == null) return invokeRaw(obj, args, methodProxy);

        val option = WestCacheOptions.newBuilder().build(ann);
        return cacheGet(option, obj, method, args, methodProxy);
    }

    @SneakyThrows
    private Object invokeRaw(final Object obj,
                             final Object[] args,
                             final MethodProxy methodProxy) {
        return target != null
                ? methodProxy.invoke(target, args)
                : methodProxy.invokeSuper(obj, args);
    }

    @SneakyThrows
    private Object cacheGet(final WestCacheOptions option,
                            final Object obj,
                            final Method method,
                            final Object[] args,
                            final MethodProxy proxy) {
        val cacheKey = option.getKeyStrategy().getCacheKey(option,
                method, target != null ? target : obj, args);

        val start = System.currentTimeMillis();
        @Cleanup val i = new Closeable() {
            @Override public void close() {
                val end = System.currentTimeMillis();
                log.debug("get cache {} cost {} millis", cacheKey, (end - start));
            }
        };

        val optional = option.getManager().get(option, cacheKey,
                new Callable<Optional<Object>>() {
                    @Override public Optional<Object> call() {
                        Object raw = invokeRaw(obj, args, proxy);
                        return Optional.fromNullable(raw);
                    }
                });
        return optional.orNull();
    }
}
