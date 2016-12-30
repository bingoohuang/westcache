package com.github.bingoohuang.westcache.outofbox;

import com.github.bingoohuang.westcache.WestCacheable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/30.
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WestCacheable(snapshot = "file", keyer = "packagelimit", flusher = "table")
public @interface MallCacheable {
}
