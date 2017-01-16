package com.github.bingoohuang.westcache.utils;

import com.google.common.cache.Cache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.SneakyThrows;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/16.
 */
public abstract class Guavas {
    @SneakyThrows
    public static <K, V> V cacheGet(Cache<K, V> cache, K k, Callable<V> callable) {
        try {
            return cache.get(k, callable);
        } catch (ExecutionException e) {
            throw e.getCause();
        } catch (UncheckedExecutionException e) {
            throw e.getCause();
        }
    }
}
