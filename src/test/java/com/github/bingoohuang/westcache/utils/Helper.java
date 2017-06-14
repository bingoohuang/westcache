package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import com.github.bingoohuang.westcache.outofbox.TableCacheFlusher;
import lombok.experimental.UtilityClass;
import lombok.val;

import static com.github.bingoohuang.westcache.WestCacheRegistry.FLUSHER_REGISTRY;
import static com.github.bingoohuang.westcache.WestCacheRegistry.REGISTRY_TEMPLATE;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/6.
 */
@UtilityClass
public class Helper {
    public TableCacheFlusher setupTableFlusherForTest() {
        val flusher = (TableCacheFlusher) FLUSHER_REGISTRY.get("table");
        flusher.cancelRotateChecker();
        flusher.getDao().setup();

        REGISTRY_TEMPLATE.deregister("default");
        REGISTRY_TEMPLATE.register("default", new DefaultWestCacheConfig() {
            @Override public long rotateIntervalMillis() {
                return 500;
            }
        });
        return flusher;
    }

    public void waitFlushRun(TableCacheFlusher flusher, long lastExecuted) {
        do {
            Envs.sleepMillis(100L);
        } while (flusher.getLastExecuted() == lastExecuted);
    }

    public static void upgradeVersion(String cacheKey,
                                      TableCacheFlusher flusher) {
        val lastExecuted = flusher.getLastExecuted();
        flusher.getDao().upgradeVersion(cacheKey);
        waitFlushRun(flusher, lastExecuted);
    }

    public static void updateDirectValue(String cacheKey,
                                         TableCacheFlusher flusher,
                                         String directValue) {
        val lastExecuted = flusher.getLastExecuted();
        flusher.getDao().updateDirectValue(cacheKey, directValue);
        waitFlushRun(flusher, lastExecuted);
    }

    public static void addBeanAndUpdateDirectValue(
            String cacheKey,
            TableCacheFlusher flusher,
            String directValue,
            WestCacheFlusherBean bean) {
        val lastExecuted = flusher.getLastExecuted();
        flusher.getDao().addBean(bean);
        flusher.getDao().updateDirectValue(cacheKey, directValue);
        waitFlushRun(flusher, lastExecuted);
    }

    public static void addConfigBean(TableCacheFlusher flusher,
                                     WestCacheFlusherBean bean) {
        long lastExecuted = flusher.getLastExecuted();
        flusher.getDao().addBean(bean);
        waitFlushRun(flusher, lastExecuted);
    }


    public static void disableConfigBean(TableCacheFlusher flusher,
                                         WestCacheFlusherBean bean) {
        long lastExecuted = flusher.getLastExecuted();
        flusher.getDao().disableBean(bean);
        waitFlushRun(flusher, lastExecuted);
    }
}
