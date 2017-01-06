package com.github.bingoohuang.westcache;

import lombok.SneakyThrows;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/6.
 */
public class ScheduledExecutorServiceCancelTest {
    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    static ScheduledFuture<?> t;

    static class MyTask implements Runnable {
        private int attempt = 1;

        public void run() {
            System.out.print(attempt + " ");
            if (++attempt > 5) {
                t.cancel(false);
            }
        }
    }

    @Test @SneakyThrows
    public void test() {
        t = executor.scheduleAtFixedRate(new MyTask(), 0, 10, TimeUnit.MILLISECONDS);
        while (!t.isDone()) {
            Thread.sleep(50);
        }
    }
}
