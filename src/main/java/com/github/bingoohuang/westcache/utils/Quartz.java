package com.github.bingoohuang.westcache.utils;

import lombok.SneakyThrows;
import lombok.val;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;

import java.util.Properties;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/16.
 */
public class Quartz {
    public Scheduler createQuartzScheduler() throws SchedulerException {
        val properties = new Properties();
        properties.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
        properties.setProperty("org.quartz.threadPool.class", SimpleThreadPool.class.getName());
        properties.setProperty("org.quartz.threadPool.threadCount", "1");
        properties.setProperty("org.quartz.jobStore.class", RAMJobStore.class.getName());

        val schedulerFactory = new StdSchedulerFactory(properties);
        return schedulerFactory.getScheduler();
    }

    private volatile Scheduler scheduler;


    @SneakyThrows
    public void scheduleJob(JobDetail jobDetail, Trigger trigger) {
        start();

        scheduler.scheduleJob(jobDetail, trigger);
    }


    @SneakyThrows
    private void start() {
        if (scheduler != null) return;

        synchronized (this) {
            if (scheduler != null) return;

            scheduler = createQuartzScheduler();
            scheduler.start();
        }
    }

    @SneakyThrows
    public void stop() {
        if (scheduler == null) return;

        scheduler.shutdown();
        scheduler = null;
    }
}
