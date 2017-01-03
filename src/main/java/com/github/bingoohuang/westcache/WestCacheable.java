package com.github.bingoohuang.westcache;

import java.lang.annotation.*;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/21.
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WestCacheable {
    /**
     * try to read from snapshot when read big data is slower than timeout.
     *
     * @return snapshot supported or not.
     */
    String snapshot() default "bypass";

    /**
     * set cache flusher's name.
     *
     * @return flusher's name.
     */
    String flusher() default "bypass";

    /**
     * set cache manager's name.
     *
     * @return manager's name.
     */
    String manager() default "guava";

    /**
     * set cache config's name.
     *
     * @return cache config's name.
     */
    String config() default "default";

    /**
     * set interceptor name for value callable.
     * @return
     */
    String interceptor() default "bypass";

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
    String keyer() default "default";

    /**
     * some specification for customized process.
     *
     * @return custom specification.
     */
    String specs() default "";
}
