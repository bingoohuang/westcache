package com.github.bingoohuang.westcache.flusher;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/*
ORACLE SQL:

 DROP TABLE WESTCACHE_FLUSHER;
 CREATE TABLE WESTCACHE_FLUSHER(
    CACHE_KEY VARCHAR2(2000 BYTE) NOT NULL PRIMARY KEY,
	KEY_MATCH VARCHAR2(20 BYTE) DEFAULT 'full' NOT NULL,
	VALUE_VERSION NUMBER DEFAULT 0 NOT NULL,
	CACHE_STATE NUMBER DEFAULT 1 NOT NULL,
	VALUE_TYPE VARCHAR2(20 BYTE) DEFAULT 'none' NOT NULL,
	DIRECT_VALUE LONG
   ) ;

   COMMENT ON COLUMN WESTCACHE_FLUSHER.CACHE_KEY IS 'cache key';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.KEY_MATCH IS 'full:full match,prefix:prefix match';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.VALUE_VERSION IS 'version of cache, increment it to update cache';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.DIRECT_VALUE IS 'direct json value for the cache';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.CACHE_STATE IS '0 disabled 1 enabled';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.VALUE_TYPE IS 'value access type, direct: use direct json in DIRECT_VALUE field';

MySql SQL:
   DROP TABLE IF EXISTS WESTCACHE_FLUSHER;
   CREATE TABLE WESTCACHE_FLUSHER(
    CACHE_KEY VARCHAR(2000) NOT NULL PRIMARY KEY COMMENT 'cache key',
	KEY_MATCH VARCHAR(20) DEFAULT 'full' NOT NULL COMMENT 'full:full match,filename:file name match, regex:regex match ant:ant path match',
	VALUE_VERSION TINYINT DEFAULT 0 NOT NULL COMMENT 'version of cache, increment it to update cache',
	CACHE_STATE TINYINT DEFAULT 1 NOT NULL COMMENT 'direct json value for the cache',
	VALUE_TYPE VARCHAR(20) DEFAULT 'none' NOT NULL COMMENT 'value access type, direct: use direct json in DIRECT_VALUE field',
	DIRECT_VALUE TEXT
   ) ;

 */

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public abstract class TableBasedCacheFlusher extends SimpleCacheFlusher {
    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    Lock readLock = readWriteLock.readLock();
    Lock writeLock = readWriteLock.writeLock();
    List<WestCacheFlusherBean> tableRows = Lists.newArrayList();
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

        val bean = findBean(cacheKey);
        return bean != null;
    }

    @Override
    public <T> T getDirectValue(WestCacheOption option, String cacheKey) {
        val bean = findBean(cacheKey);
        if (bean == null) return null;

        if (!"direct".equals(bean.getValueType())) return null;

        if ("full".equals(bean.getKeyMatch())) {
            return (T) readDirectValue(bean.getCacheKey(), null);
        } else if ("prefix".equals(bean.getKeyMatch())) {
            val subKey = cacheKey.substring(bean.getCacheKey().length() + 1);
            return (T) readDirectValue(bean.getCacheKey(), subKey);
        }

        return null;
    }

    private WestCacheFlusherBean findBean(String cacheKey) {
        for (val bean : tableRows) {
            if ("full".equals(bean.getKeyMatch())) {
                if (bean.getCacheKey().equals(cacheKey)) return bean;
            } else if ("prefix".equals(bean.getKeyMatch())) {
                if (cacheKey.startsWith(bean.getCacheKey())) return bean;
            }
        }
        return null;
    }

    private void startupRotateChecker(WestCacheOption option) {
        lastExecuted = 0;
        val config = option.getConfig();

        checkBeans();
        long intervalMillis = config.rotateCheckIntervalMillis();
        config.executorService().scheduleAtFixedRate(new Runnable() {
            @Override public void run() {
                checkBeans();
            }
        }, intervalMillis, intervalMillis, MILLISECONDS);
    }


    @SneakyThrows
    private void checkBeans() {
        val beans = queryAllBeans();
        if (beans.equals(tableRows)) return;

        diff(tableRows, beans);

        writeLock.lock();
        @Cleanup val i = new Closeable() {
            @Override public void close() throws IOException {
                writeLock.unlock();
            }
        };

        tableRows = beans;
        lastExecuted = System.currentTimeMillis();
    }

    protected abstract List<WestCacheFlusherBean> queryAllBeans();

    protected abstract Object readDirectValue(String cacheKey, String subKey);

    private void diff(List<WestCacheFlusherBean> table,
                      List<WestCacheFlusherBean> beans) {
        Map<String, WestCacheFlusherBean> flushKeys = Maps.newHashMap();
        for (val bean : table) {
            val found = find(bean, beans);
            if (found == null ||
                    found.getValueVersion() != bean.getValueVersion()) {
                flushKeys.put(bean.getCacheKey(), bean);
            }
        }

        for (val key : getRegistry().asMap().keySet()) {
            if (flushKeys.containsKey(key)) {
                flush(key);
            } else {
                for (val bean : flushKeys.values()) {
                    if (!"prefix".equals(bean.getKeyMatch())) continue;
                    if (key.startsWith(bean.getCacheKey())) flush(key);
                }
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