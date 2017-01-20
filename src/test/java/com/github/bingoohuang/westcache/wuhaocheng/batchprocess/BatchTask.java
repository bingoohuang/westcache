package com.github.bingoohuang.westcache.wuhaocheng.batchprocess;

import com.google.common.util.concurrent.SettableFuture;
import lombok.Getter;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/20.
 */
public class BatchTask<T, V> {
    @Getter private final T arg;
    @Getter private final SettableFuture<V> settableFuture;

    public BatchTask(T arg, SettableFuture<V> settableFuture) {
        this.arg = arg;
        this.settableFuture = settableFuture;
    }
}
