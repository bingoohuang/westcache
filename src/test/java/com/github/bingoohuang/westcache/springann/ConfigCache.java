package com.github.bingoohuang.westcache.springann;

import com.github.bingoohuang.westcache.WestCacheable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/17.
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WestCacheable(manager = "configExpiring", keyer = "simple", specs = "ttlConfigKey=ttlConfig")
public @interface ConfigCache {
}
