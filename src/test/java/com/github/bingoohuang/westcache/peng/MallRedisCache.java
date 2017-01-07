package com.github.bingoohuang.westcache.peng;

import com.github.bingoohuang.westcache.WestCacheable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@WestCacheable(manager = "redis", keyer = "packagelimit", flusher = "table")
public @interface MallRedisCache {
    String key() default "";

    String expireAfterWrite() default "";

    String redisBean() default "singleRedis";
}
