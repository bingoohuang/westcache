package com.github.bingoohuang.westcache.batch;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/20.
 */
@Slf4j
public class Batcher<T, V> {
    final Queue<BatcherBean<T, V>> queue;
    final ScheduledExecutorService service;
    final BatcherJob<T, V> batcherJob;
    final int maxWaitItems;
    private final Runnable runner;

    public Batcher(final ScheduledExecutorService service,
                   final BatcherJob<T, V> batcherJob,
                   final long maxWaitMillis,
                   final int maxWaitItems,
                   final int maxBatchNum) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.service = service;
        this.batcherJob = batcherJob;
        this.maxWaitItems = maxWaitItems;
        this.runner = () -> {
            if (queue.isEmpty()) return;

            val tasks = new ArrayList<BatcherBean<T, V>>(queue.size());
            do {
                val task = queue.poll();
                if (task == null) break;

                tasks.add(task);
            } while (tasks.size() != maxBatchNum);

            if (tasks.isEmpty()) return;

            doBatchWork(tasks);
        };
        service.scheduleWithFixedDelay(
                runner, maxWaitMillis, maxWaitMillis, TimeUnit.MILLISECONDS);
    }


    /**
     * Submit a argument to the batcher.
     *
     * @param argument batch argument.
     * @return future object.
     */
    public Future<V> submit(T argument) {
        val future = SettableFuture.<V>create();
        queue.add(new BatcherBean(argument, future));
        if (queue.size() < maxWaitItems) return future;

        service.execute(runner);
        return future;
    }

    private void doBatchWork(List<BatcherBean<T, V>> tasks) {
        List<T> batchArgs = Lists.transform(tasks, task -> task.getArg());

        List<V> results = null;
        Exception ex = null;
        try {
            results = batcherJob.doBatchJob(batchArgs);
        } catch (Exception e) {
            log.error("call doBatchJob err", e);
            ex = e;
        }

        int resultsSize = results != null ? results.size() : 0;
        if (resultsSize != tasks.size()) {
            log.error("result size {} is not same with task size {}", resultsSize, tasks.size());
        }

        futuresSet(tasks, results, ex, resultsSize);
    }

    private void futuresSet(List<BatcherBean<T, V>> tasks, List<V> results, Exception ex, int resultsSize) {
        for (int i = 0, ii = tasks.size(); i < ii; ++i) {
            val future = tasks.get(i).getFuture();
            if (i < resultsSize) {
                future.set(results.get(i));
            } else {
                future.setException(ex != null ? ex : new RuntimeException("result is not available"));
            }
        }
    }

    /**
     * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/22.
     */
    @AllArgsConstructor @Value private static class BatcherBean<T, V> {
        private final T arg;
        private final SettableFuture<V> future;
    }
}
