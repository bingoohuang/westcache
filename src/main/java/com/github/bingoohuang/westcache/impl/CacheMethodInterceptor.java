package com.github.bingoohuang.westcache.impl;

import com.github.bingoohuang.westcache.WestCacheOptions;
import com.github.bingoohuang.westcache.utils.WestCacheAnns;
import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

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
                            final MethodProxy methodProxy) throws Throwable {
        val option = WestCacheAnns.parseWestCacheOption(method);
        return option == null
                ? invokeRaw(obj, args, methodProxy)
                : cacheGet(option, obj, method, args, methodProxy);
    }

    @SneakyThrows
    private Object invokeRaw(final Object obj,
                             final Object[] args,
                             final MethodProxy methodProxy) {
        return target != null
                ? methodProxy.invoke(target, args)
                : methodProxy.invokeSuper(obj, args);
    }

    private Object cacheGet(final WestCacheOptions option,
                            final Object obj,
                            final Method method,
                            final Object[] args,
                            final MethodProxy proxy) {
        val cacheKey = option.getKeyStrategy().getCacheKey(option,
                method, target != null ? target : obj, args);
        val start = System.currentTimeMillis();
        try {
            return option.getSnapshot() != null
                    ? snapshotRead(option, obj, method, args, proxy, cacheKey)
                    : normalRead(option, obj, method, args, proxy, cacheKey);
        } finally {
            val end = System.currentTimeMillis();
            log.debug("get cache {} cost {} millis", cacheKey, (end - start));
        }
    }

    @SneakyThrows
    private Object snapshotRead(final WestCacheOptions option,
                                final Object obj,
                                final Method method,
                                final Object[] args,
                                final MethodProxy proxy,
                                final String cacheKey) {
        Optional<Object> o = option.getManager()
                .getSnapshot(option, cacheKey,
                        new Callable<Optional<Object>>() {
                            @SneakyThrows @Override
                            public Optional<Object> call() throws Exception {
                                Object raw = invokeRaw(obj, args, proxy);
                                return Optional.fromNullable(raw);
                            }
                        });
        return o.orNull();
    }

    @SneakyThrows
    private Object normalRead(final WestCacheOptions option,
                              final Object obj,
                              final Method method,
                              final Object[] args,
                              final MethodProxy proxy,
                              final String cacheKey) {
        Optional<Object> o = option.getManager().get(option, cacheKey,
                new Callable<Optional<Object>>() {
                    @SneakyThrows @Override
                    public Optional<Object> call() throws Exception {
                        Object raw = invokeRaw(obj, args, proxy);
                        return Optional.fromNullable(raw);
                    }
                });
        return o.orNull();
    }
}
