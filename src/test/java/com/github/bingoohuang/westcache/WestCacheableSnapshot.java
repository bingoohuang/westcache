package com.github.bingoohuang.westcache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@WestCacheable(snapshot = true)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WestCacheableSnapshot {
}
