package com.github.bingoohuang.westcache.impl;

import com.github.bingoohuang.westcache.WestCacheGuava;
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

import static com.github.bingoohuang.westcache.utils.CacheAnnotationUtils.parseWestCacheableOption;
import static com.github.bingoohuang.westcache.utils.CacheKeyUtils.createCacheKey;

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
        val option = parseWestCacheableOption(method);
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

    private Object cacheGet(final WestCacheOption option,
                            final Object obj,
                            final Method method,
                            final Object[] args,
                            final MethodProxy proxy) {
        val start = System.currentTimeMillis();
        try {
            return option.isSnapshot()
                    ? snapshotRead(obj, method, args, proxy)
                    : normalRead(obj, method, args, proxy);
        } finally {
            val end = System.currentTimeMillis();
            String cacheKey = createCacheKey(method);
            log.debug("get cache {} cost {} millis", cacheKey, (end - start));
        }
    }

    @SneakyThrows
    private Object snapshotRead(final Object obj,
                                final Method method,
                                final Object[] args,
                                final MethodProxy proxy) {
        val cacheKey = createCacheKey(method);

        Optional<Object> o = WestCacheGuava.getSnapshot(cacheKey,
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
    private Object normalRead(final Object obj,
                              final Method method,
                              final Object[] args,
                              final MethodProxy proxy) {
        val cacheKey = createCacheKey(target != null ? target : obj, method, args);

        Optional<Object> o = WestCacheGuava.get(cacheKey,
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
