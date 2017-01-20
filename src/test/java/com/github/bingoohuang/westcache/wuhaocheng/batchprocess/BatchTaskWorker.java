package com.github.bingoohuang.westcache.wuhaocheng.batchprocess;

import java.util.List;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/20.
 */
public interface BatchTaskWorker<T, V> {
    List<V> doBatchTasks(List<T> batchArgs);
}
