package com.github.bingoohuang.westcache;

import java.lang.annotation.*;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/21.
 */
@Documented
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WestCacheable {
    /**
     * try to read from snapshot when read big data is slower than timeout.
     *
     * @return snapshot supported or not.
     */
    boolean snapshot() default false;
}
