package com.github.bingoohuang.westcache.flusher;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
 DROP TABLE WESTCACHE_FLUSHER;
 CREATE TABLE WESTCACHE_FLUSHER(
    CACHE_KEY VARCHAR2(2000 BYTE) NOT NULL,
	KEY_MATCH VARCHAR2(20 BYTE) DEFAULT 'full' NOT NULL,
	VALUE_VERSION NUMBER DEFAULT 0 NOT NULL,
	CACHE_STATE NUMBER DEFAULT 1 NOT NULL,
	VALUE_TYPE VARCHAR2(20 BYTE) DEFAULT 'none' NOT NULL,
	DIRECT_VALUE LONG,
	CONSTRAINT WESTCACHE_FLUSHER_PK PRIMARY KEY (CACHE_KEY)
   ) ;

   COMMENT ON COLUMN WESTCACHE_FLUSHER.CACHE_KEY IS 'cache key';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.KEY_MATCH IS 'full:full match,filename:file name match, regex:regex match ant:ant path match';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.VALUE_VERSION IS 'version of cache, increment it to update cache';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.DIRECT_VALUE IS 'direct json value for the cache';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.CACHE_STATE IS '0 disabled 1 enabled';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.VALUE_TYPE IS 'value access type, direct: use direct json in DIRECT_VALUE field';
 */

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public abstract class TableBasedCacheFlusher extends SimpleCacheFlusher {
    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    Lock readLock = readWriteLock.readLock();
    Lock writeLock = readWriteLock.writeLock();
    List<WestCacheFlusherBean> table = Lists.newArrayList();
    @Getter volatile long lastExecuted = -1;

    @Override @SneakyThrows
    public boolean isKeyEnabled(WestCacheOption option, String cacheKey) {
        if (lastExecuted == -1) startupRotateChecker(option);

        readLock.lock();
        @Cleanup val i = new Closeable() {
            @Override public void close() throws IOException {
                readLock.unlock();
            }
        };

        for (val bean : table) {
            if (bean.getCacheKey().equals(cacheKey)) return true;
        }

        return false;
    }

    private void startupRotateChecker(WestCacheOption option) {
        lastExecuted = 0;
        option.getConfig().executorService().scheduleAtFixedRate(new Runnable() {
            @Override public void run() {
                checkBeans();
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }

    @SneakyThrows
    private void checkBeans() {
        val beans = queryAllBeans();
        if (beans.equals(table)) return;

        diff(table, beans);

        writeLock.lock();
        @Cleanup val i = new Closeable() {
            @Override public void close() throws IOException {
                writeLock.unlock();
            }
        };

        table = beans;
        lastExecuted = System.currentTimeMillis();
    }

    protected abstract List<WestCacheFlusherBean> queryAllBeans();

    private void diff(List<WestCacheFlusherBean> table,
                      List<WestCacheFlusherBean> beans) {
        Set<String> flushKeys = Sets.newHashSet();
        for (val bean : table) {
            val found = find(bean, beans);
            if (found == null ||
                    found.getValueVersion() != bean.getValueVersion()) {
                flushKeys.add(bean.getCacheKey());
            }
        }

        for (val key : getRegistry().asMap().keySet()) {
            if (flushKeys.contains(key)) {
                flush(key);
            }
        }
    }

    private WestCacheFlusherBean find(
            WestCacheFlusherBean bean,
            List<WestCacheFlusherBean> beans) {
        for (val newbean : beans) {
            if (bean.getCacheKey().equals(newbean.getCacheKey()))
                return newbean;
        }
        return null;
    }
}