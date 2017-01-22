package com.github.bingoohuang.westcache.batch;

import java.util.List;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/20.
 */
public interface BatcherJob<T, V> {
    /**
     * do batch job with multiple arguments.
     *
     * @param batchArgs multiple arguments
     * @return multiple results
     */
    List<V> doBatchJob(List<T> batchArgs);
}
