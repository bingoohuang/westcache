package com.github.bingoohuang.westcache.wuhaocheng.batchprocess;

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
public class BatchTaskManager<T, V> {
    final Queue<BatchTask<T, V>> queue;
    final ScheduledExecutorService service;
    final BatchTaskWorker<T, V> batchTaskWorker;
    final long maxWaitMillis;
    final int maxWaitItems;
    final int maxBatchNum;

    final Runnable runnable = new Runnable() {
        @Override public void run() {
            if (queue.isEmpty()) return;

            val tasks = new ArrayList<BatchTask<T, V>>(queue.size());
            while (true) {
                val task = queue.poll();
                if (task == null) break;
                tasks.add(task);
                if (tasks.size() == maxBatchNum) break;
            }

            if (tasks.isEmpty()) return;

            doBatchWork(tasks);
        }
    };


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
        service.scheduleWithFixedDelay(runnable,
                maxWaitMillis, maxWaitMillis, TimeUnit.MILLISECONDS);
    }

    public Future<V> enroll(T tokenId) {
        val settableFuture = SettableFuture.<V>create();
        queue.add(new BatchTask(tokenId, settableFuture));
        if (queue.size() > maxWaitItems) {
            service.execute(runnable);
        }

        return settableFuture;
    }

    private void doBatchWork(List<BatchTask<T, V>> tasks) {
        int taskSize = tasks.size();
        val batchArgs = new ArrayList<T>(taskSize);
        for (val task : tasks) {
            batchArgs.add(task.getArg());
        }

        val results = batchTaskWorker.doBatchTasks(batchArgs);
        int resultsSize = results != null ? results.size() : 0;
        if (resultsSize != taskSize) {
            log.warn("result size {} is not same with task size {}",
                    resultsSize, taskSize);
        }

        for (int i = 0, ii = taskSize; i < ii; ++i) {
            V result = i < resultsSize ? results.get(i) : null;
            tasks.get(i).getSettableFuture().set(result);
        }
    }

}
