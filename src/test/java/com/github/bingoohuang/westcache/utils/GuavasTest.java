package com.github.bingoohuang.westcache.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/20.
 */
public class GuavasTest {
    @Test
    public void executionException() {
        Cache<String, String> cache = CacheBuilder.newBuilder().build();
        try {
            Guavas.cacheGet(cache, "bingoo", new Callable<String>() {
                @Override public String call() throws Exception {
                    throw new Exception("for test");
                }
            });
        } catch (Exception ex) {
            assertThat(ex.getMessage()).isEqualTo("for test");
            return;
        }
        Assert.fail();
    }

    @Test
    public void uncheckedExecutionException() {
        Cache<String, String> cache = CacheBuilder.newBuilder().build();
        try {
            Guavas.cacheGet(cache, "bingoo", new Callable<String>() {
                @Override public String call() {
                    throw new RuntimeException("for test");
                }
            });
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("for test");
            return;
        }
        Assert.fail();
    }

    @Test
    public void getUncheckedException() {
        LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, String>() {
                    @Override public String load(String key) throws Exception {
                        throw new Exception("for test");
                    }
                });
        try {
            Guavas.cacheGet(cache, "bingoo");
        } catch (Exception ex) {
            assertThat(ex.getMessage()).isEqualTo("for test");
            return;
        }
        Assert.fail();
    }
}
