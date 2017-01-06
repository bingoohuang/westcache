package com.github.bingoohuang.westcache;

import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/6.
 */
public class ScheduledExecutorServiceCancelTest {
    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    static ScheduledFuture<?> t;

    static class MyTask implements Runnable {
        @Getter private volatile int attempt = 0;

        public void run() {
            if (++attempt > 5) {
                t.cancel(false);
            }
        }
    }

    @Test @SneakyThrows
    public void test() {
        MyTask task = new MyTask();
        t = executor.scheduleAtFixedRate(task, 0, 10, TimeUnit.MILLISECONDS);
        while (!t.isDone()) {
            Thread.sleep(50);
        }
        assertThat(task.getAttempt()).isEqualTo(6);

    }
}
