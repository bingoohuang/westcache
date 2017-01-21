package com.github.bingoohuang.westcache.wuhaocheng.batchprocess;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newScheduledThreadPool;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/20.
 */
public class BatchTaskManagerBuilder<T, V> {
    private final BatchTaskWorker<T, V> batchTaskWorker;

    Queue<BatchTask<T, V>> queue;
    ScheduledExecutorService service;
    int maxWaitItems = 10; // 达到多少个就开工
    long maxWaitMillis = 300; // 或者累计满多少毫秒也开工
    int maxBatchNum = 0; // 一批最多多少个，<=0 不限制

    public BatchTaskManagerBuilder(BatchTaskWorker<T, V> batchTaskWorker) {
        this.batchTaskWorker = batchTaskWorker;
    }

    public static <T, V> BatchTaskManagerBuilder<T, V> newBuilder(
            BatchTaskWorker<T, V> batchTaskWorker) {
        return new BatchTaskManagerBuilder<T, V>(batchTaskWorker);
    }

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

    public BatchTaskManager<T, V> build() {
        if (queue == null) queue = new ConcurrentLinkedQueue<BatchTask<T, V>>();
        if (service == null) service = newScheduledThreadPool(10);
        return new BatchTaskManager(queue, service, batchTaskWorker,
                maxWaitMillis, maxWaitItems, maxBatchNum);
    }
}
