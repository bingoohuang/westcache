package com.github.bingoohuang.westcache.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/4.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(WestCacheableSpringConfig.class)
public @interface WestCacheableEnabled {
}
