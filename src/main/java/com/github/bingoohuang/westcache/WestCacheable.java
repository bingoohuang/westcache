package com.github.bingoohuang.westcache;

import java.lang.annotation.*;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/21.
 */
@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WestCacheable {
    /**
     * try to read from snapshot when read big data is slower than timeout.
     *
     * @return snapshot supported or not.
     */
    String snapshot() default "";

    /**
     * set cache flusher's name.
     *
     * @return flusher's name.
     */
    String flusher() default "";

    /**
     * set cache manager's name.
     *
     * @return manager's name.
     */
    String manager() default "";

    /**
     * set cache config's name.
     *
     * @return cache config's name.
     */
    String config() default "";

    /**
     * set interceptor name for value callable.
     *
     * @return interceptor's name.
     */
    String interceptor() default "";

    /**
     * set cache's key.
     *
     * @return cache's key.
     */
    String key() default "";

    /**
     * set key strategy's name.
     *
     * @return key strategy's name.
     */
    String keyer() default "";

    /**
     * some specification for customized process.
     *
     * @return custom specification.
     */
    String specs() default "";
}
