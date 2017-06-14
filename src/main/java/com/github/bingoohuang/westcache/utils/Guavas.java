package com.github.bingoohuang.westcache.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/16.
 */
@Slf4j @UtilityClass
public class Guavas {
    @SneakyThrows
    public static <K, V> V cacheGet(Cache<K, V> cache, K k, Callable<V> callable) {
        try {
            return cache.get(k, callable);
        } catch (ExecutionException e) {
            log.warn("cache get ExecutionException", e);
            throw e.getCause();
        } catch (UncheckedExecutionException e) {
            log.warn("cache get UncheckedExecutionException", e);
            throw e.getCause();
        }
    }

    @SneakyThrows
    public static <K, V> V cacheGet(LoadingCache<K, V> cache, K k) {
        try {
            return cache.getUnchecked(k);
        } catch (UncheckedExecutionException e) {
            log.warn("cache get UncheckedExecutionException", e);
            throw e.getCause();
        }
    }
}
