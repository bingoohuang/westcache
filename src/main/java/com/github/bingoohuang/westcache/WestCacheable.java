package com.github.bingoohuang.westcache;

import java.lang.annotation.*;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/21.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WestCacheable {
    /**
     * try to read from snapshot when read big data is slower than timeout.
     * @return
     */
    boolean snapshot() default false;
}
