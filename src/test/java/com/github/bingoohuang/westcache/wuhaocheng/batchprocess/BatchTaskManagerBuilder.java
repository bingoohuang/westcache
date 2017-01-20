package com.github.bingoohuang.westcache.wuhaocheng.batchprocess;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/20.
 */
public class BatchTaskManagerBuilder<T, V> {
    Queue<BatchTask<T, V>> queue;
    ScheduledExecutorService service;
    long maxWaitMillis = 1;
    int maxWaitItems = 10;
    int maxBatchNum = 0;

    public BatchTaskManagerBuilder<T, V> queue(Queue<BatchTask<T, V>> queue) {
        this.queue = queue;
        return this;
    }

    public BatchTaskManagerBuilder<T, V> executor(ScheduledExecutorService service) {
        this.service = service;
        return this;
    }

    public BatchTaskManagerBuilder<T, V> maxWaitMillis(long maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
        return this;
    }

    public BatchTaskManagerBuilder<T, V> maxWaitItems(int maxWaitItems) {
        this.maxWaitItems = maxWaitItems;
        return this;
    }

    public BatchTaskManagerBuilder<T, V> maxBatchNum(int maxBatchNum) {
        this.maxBatchNum = maxBatchNum;
        return this;
    }

    public BatchTaskManager<T, V> build(BatchTaskWorker<T, V> batchTaskWorker) {
        if (queue == null) queue = new ConcurrentLinkedQueue<BatchTask<T, V>>();
        if (service == null) service = Executors.newScheduledThreadPool(10);
        return new BatchTaskManager(queue, service, batchTaskWorker, maxWaitMillis, maxWaitItems, maxBatchNum);
    }
}
