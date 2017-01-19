package com.github.bingoohuang.westcache.base;

import java.lang.annotation.*;

/**
 * Annotation to tag a method which returns a duration.
 *
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/19.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpireAfterWrite {
}
