package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.utils.Envs;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/6.
 */
public class RedisManagerExpireCustomAnnTest {
    @Retention(RetentionPolicy.RUNTIME)
    @WestCacheable(manager = "redis", keyer = "simple")
    @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    public @interface RedisCache {
        String expireAfterWrite();
    }

    public static volatile int calledTimes = 0;

    public static class RedisCacheService {
        @RedisCache(expireAfterWrite = "1s")
        public long getTimestamp() {
            ++calledTimes;
            return System.currentTimeMillis();
        }
    }

    static RedisCacheService service = WestCacheFactory.create(RedisCacheService.class);

    @Test
    public void test() {
        service.getTimestamp();
        service.getTimestamp();
        Envs.sleepMillis(1000L);
        service.getTimestamp();
        assertThat(calledTimes).isEqualTo(2);
    }
}
