package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import com.github.bingoohuang.westcache.outofbox.TableCacheFlusher;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import static com.github.bingoohuang.westcache.WestCacheRegistry.configRegistry;
import static com.github.bingoohuang.westcache.WestCacheRegistry.flusherRegistry;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/6.
 */
@UtilityClass
public class Helper {
    public TableCacheFlusher setupTableFlusherForTest() {
        val flusher = (TableCacheFlusher) flusherRegistry.get("table");
        flusher.cancelRotateChecker();
        flusher.getDao().setup();

        configRegistry.deregister("default");
        configRegistry.register("default", new DefaultWestCacheConfig() {
            @Override public long rotateIntervalMillis() {
                return 500;
            }
        });
        return flusher;
    }

    @SneakyThrows
    public void waitFlushRun(TableCacheFlusher flusher, long lastExecuted) {
        do {
            Thread.sleep(100L);
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
}
