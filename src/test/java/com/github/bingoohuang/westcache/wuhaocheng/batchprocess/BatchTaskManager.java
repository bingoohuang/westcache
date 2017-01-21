package com.github.bingoohuang.westcache.wuhaocheng.batchprocess;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/20.
 */
@Slf4j
public class BatchTaskManager<T, V> implements Runnable {
    final Queue<BatchTask<T, V>> queue;
    final ScheduledExecutorService service;
    final BatchTaskWorker<T, V> batchTaskWorker;
    final long maxWaitMillis;
    final int maxWaitItems;
    final int maxBatchNum;

    @Override public void run() {
        if (queue.isEmpty()) return;

        val tasks = new ArrayList<BatchTask<T, V>>(queue.size());
        do {
            val task = queue.poll();
            if (task == null) break;

            tasks.add(task);
        } while (tasks.size() != maxBatchNum);

        if (tasks.isEmpty()) return;

        doBatchWork(tasks);
    }

    public BatchTaskManager(Queue<BatchTask<T, V>> queue,
                            ScheduledExecutorService service,
                            BatchTaskWorker<T, V> batchTaskWorker,
                            long maxWaitSeconds,
                            int maxWaitItems, int maxBatchNum) {
        this.queue = queue;
        this.service = service;
        this.batchTaskWorker = batchTaskWorker;
        this.maxWaitMillis = maxWaitSeconds;
        this.maxWaitItems = maxWaitItems;
        this.maxBatchNum = maxBatchNum;
        service.scheduleWithFixedDelay(this,
                maxWaitMillis, maxWaitMillis, TimeUnit.MILLISECONDS);
    }

    public Future<V> enroll(T tokenId) {
        val future = SettableFuture.<V>create();
        queue.add(new BatchTask(tokenId, future));
        if (queue.size() >= maxWaitItems) service.execute(this);

        return future;
    }

    private void doBatchWork(List<BatchTask<T, V>> tasks) {
        val batchArgs = Lists.transform(tasks,
                new Function<BatchTask<T, V>, T>() {
                    @Override public T apply(BatchTask<T, V> task) {
                        return task.getArg();
                    }
                });

        val results = batchTaskWorker.doBatchTasks(batchArgs);
        int resultsSize = results != null ? results.size() : 0;
        if (resultsSize != tasks.size()) {
            log.warn("result size {} is not same with task size {}",
                    resultsSize, tasks.size());
        }

        for (int i = 0, ii = tasks.size(); i < ii; ++i) {
            V result = i < resultsSize ? results.get(i) : null;
            tasks.get(i).getSettableFuture().set(result);
        }
    }

}
