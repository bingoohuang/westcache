package com.github.bingoohuang.westcache.impl;

import com.github.bingoohuang.westcache.WestCacheable;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.concurrent.*;

import static com.github.bingoohuang.westcache.impl.CacheKeyUtils.createCacheKey;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/21.
 */
@Slf4j
public class CacheMethodInterceptor implements MethodInterceptor {
    private Object target;

    public CacheMethodInterceptor() {
    }

    public CacheMethodInterceptor(Object target) {
        this.target = target;
    }

    static Cache<String, Optional<Object>> cache = CacheBuilder.newBuilder().build();

    @Override
    public Object intercept(final Object obj,
                            final Method method,
                            final Object[] args,
                            final MethodProxy methodProxy) throws Throwable {

        return isWestCacheable(method)
                ? cacheGet(obj, method, args, methodProxy)
                : invokeRaw(obj, args, methodProxy);
    }

    @SneakyThrows
    private Object invokeRaw(final Object obj,
                             final Object[] args,
                             final MethodProxy methodProxy) {
        return target != null
                ? methodProxy.invoke(target, args)
                : methodProxy.invokeSuper(obj, args);
    }

    private Object cacheGet(final Object obj,
                            final Method method,
                            final Object[] args,
                            final MethodProxy proxy) {
        val start = System.currentTimeMillis();
        try {
            val westCacheable = method.getAnnotation(WestCacheable.class);
            return westCacheable.snapshot()
                    ? snapshotRead(obj, method, args, proxy)
                    : normalRead(obj, method, args, proxy);
        } finally {
            val end = System.currentTimeMillis();
            String cacheKey = createCacheKey(method);
            log.debug("get cache {} cost {} millis", cacheKey, (end - start));
        }
    }

    static ExecutorService executorService = Executors.newFixedThreadPool(10);

    @SneakyThrows
    private Object snapshotRead(final Object obj,
                                final Method method,
                                final Object[] args,
                                final MethodProxy proxy) {
        val cacheKey = createCacheKey(method);

        return cache.get(cacheKey, new Callable<Optional<Object>>() {
            @SneakyThrows @Override
            public Optional<Object> call() throws Exception {
                val future = executorService.submit(new Callable<Optional<Object>>() {
                    @Override
                    public Optional<Object> call() throws Exception {
                        Object raw = invokeRaw(obj, args, proxy);
                        val optional = Optional.fromNullable(raw);
                        cache.put(cacheKey, optional);
                        FileSnapshotUtils.saveSnapshot(cacheKey, raw);
                        return optional;
                    }
                });

                Optional<Object> result;
                try {
                    result = future.get(500, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {   // 有限时间内不返回，尝试snapshot
                    log.info("get cache {} timeout in 500 millis, try to use snapshot", cacheKey);
                    result = FileSnapshotUtils.readSnapShot(cacheKey);
                }

                return result == null ? future.get() : result;
            }
        }).orNull();
    }

    @SneakyThrows
    private Object normalRead(final Object obj,
                              final Method method,
                              final Object[] args,
                              final MethodProxy proxy) {
        val cacheKey = createCacheKey(target != null ? target : obj, method, args);

        return cache.get(cacheKey, new Callable<Optional<Object>>() {
            @SneakyThrows @Override
            public Optional<Object> call() throws Exception {
                Object raw = invokeRaw(obj, args, proxy);
                return Optional.fromNullable(raw);
            }
        }).orNull();
    }

    private boolean isWestCacheable(Method method) {
        return method.isAnnotationPresent(WestCacheable.class);
    }
}
