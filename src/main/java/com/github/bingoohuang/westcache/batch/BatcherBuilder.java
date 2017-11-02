package com.github.bingoohuang.westcache.batch;

import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newScheduledThreadPool;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/20.
 */
public class BatcherBuilder<T, V> {
    private final BatcherJob<T, V> batcherJob;

    ScheduledExecutorService service;
    int maxWaitItems = 10; // 达到多少个就开工
    long maxWaitMillis = 300; // 或者累计满多少毫秒也开工
    int maxBatchNum = 0; // 一批最多多少个，<=0 不限制

    public BatcherBuilder(BatcherJob<T, V> batcherJob) {
        this.batcherJob = batcherJob;
    }

    public static <T, V> BatcherBuilder<T, V> newBuilder(
            BatcherJob<T, V> batcherJob) {
        return new BatcherBuilder<T, V>(batcherJob);
    }

    public BatcherBuilder<T, V> executor(ScheduledExecutorService service) {
        this.service = service;
        return this;
    }

    public BatcherBuilder<T, V> maxWaitMillis(long maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
        return this;
    }

    public BatcherBuilder<T, V> maxWaitItems(int maxWaitItems) {
        this.maxWaitItems = maxWaitItems;
        return this;
    }

    public BatcherBuilder<T, V> maxBatchNum(int maxBatchNum) {
        this.maxBatchNum = maxBatchNum;
        return this;
    }

    public Batcher<T, V> build() {
        createServiceIfNotSet();
        return new Batcher(service, batcherJob, maxWaitMillis, maxWaitItems, maxBatchNum);
    }

    private void createServiceIfNotSet() {
        if (service != null) return;
        service = newScheduledThreadPool(5);
    }
}
