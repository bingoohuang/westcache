package com.github.bingoohuang.westcache.flusher;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.Keys;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

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
	SPECS VARCHAR2(2000 BYTE) NULL,
	DIRECT_VALUE LONG
   ) ;

   COMMENT ON COLUMN WESTCACHE_FLUSHER.CACHE_KEY IS 'cache key';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.KEY_MATCH IS 'full:full match,prefix:prefix match';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.VALUE_VERSION IS 'version of cache, increment it to update cache';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.DIRECT_VALUE IS 'direct json value for the cache';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.CACHE_STATE IS '0 disabled 1 enabled';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.VALUE_TYPE IS 'value access type, direct: use direct json in DIRECT_VALUE field';
   COMMENT ON COLUMN WESTCACHE_FLUSHER.SPECS IS 'specs for extension';

MySql SQL:
   DROP TABLE IF EXISTS WESTCACHE_FLUSHER;
   CREATE TABLE WESTCACHE_FLUSHER(
    CACHE_KEY VARCHAR(2000) NOT NULL PRIMARY KEY COMMENT 'cache key',
	KEY_MATCH VARCHAR(20) DEFAULT 'full' NOT NULL COMMENT 'full:full match,prefix:prefix match',
	VALUE_VERSION TINYINT DEFAULT 0 NOT NULL COMMENT 'version of cache, increment it to update cache',
	CACHE_STATE TINYINT DEFAULT 1 NOT NULL COMMENT 'direct json value for the cache',
	VALUE_TYPE VARCHAR(20) DEFAULT 'none' NOT NULL COMMENT 'value access type, direct: use direct json in DIRECT_VALUE field',
	SPECS VARCHAR(2000) NULL COMMENT 'specs for extension',
	DIRECT_VALUE TEXT
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

 */

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
@Slf4j
public abstract class TableBasedCacheFlusher extends SimpleCacheFlusher {
    volatile List<WestCacheFlusherBean> tableRows = Lists.newArrayList();
    @Getter volatile long lastExecuted = -1;
    Cache<String, Optional<Map<String, Object>>> prefixDirectCache
            = CacheBuilder.newBuilder().build();

    @Override @SneakyThrows
    public boolean isKeyEnabled(WestCacheOption option, String cacheKey) {
        tryStartup(option, cacheKey);


        val bean = findBean(cacheKey);
        return bean != null;
    }

    private void tryStartup(WestCacheOption option, String cacheKey) {
        if (lastExecuted != -1) return;

        synchronized (this) {
            if (lastExecuted != -1) return;

            startupRotateChecker(option, cacheKey);
        }
    }

    @Override
    public Optional<Object> getDirectValue(
            WestCacheOption option, String cacheKey) {
        val bean = findBean(cacheKey);
        if (bean == null) return Optional.absent();

        if (!"direct".equals(bean.getValueType())) {
            return Optional.absent();
        }

        if ("full".equals(bean.getKeyMatch())) {
            Object value = readDirectValue(bean);
            return Optional.fromNullable(value);
        }

        if ("prefix".equals(bean.getKeyMatch())) {
            val subKey = cacheKey.substring(bean.getCacheKey().length() + 1);
            Object value = readSubDirectValue(bean, subKey);
            return Optional.fromNullable(value);
        }

        return Optional.absent();
    }


    protected abstract List<WestCacheFlusherBean> queryAllBeans();

    protected abstract Object readDirectValue(WestCacheFlusherBean bean);

    @SneakyThrows
    private <T> T readSubDirectValue(
            final WestCacheFlusherBean bean, String subKey) {
        val optional = prefixDirectCache.get(bean.getCacheKey(),
                new Callable<Optional<Map<String, Object>>>() {
                    @Override
                    public Optional<Map<String, Object>> call() throws Exception {
                        val map = (Map<String, Object>) readDirectValue(bean);
                        return Optional.fromNullable(map);
                    }
                });

        return optional.isPresent() ? (T) optional.get().get(subKey) : null;
    }

    protected WestCacheFlusherBean findBean(String cacheKey) {
        for (val bean : tableRows) {
            if ("full".equals(bean.getKeyMatch())) {
                if (bean.getCacheKey().equals(cacheKey)) return bean;
            } else if ("prefix".equals(bean.getKeyMatch())) {
                if (Keys.isPrefix(cacheKey, bean.getCacheKey())) return bean;
            }
        }
        return null;
    }

    protected void startupRotateChecker(final WestCacheOption option,
                                        final String cacheKey) {
        firstCheckBeans(option, cacheKey);

        long intervalMillis = option.getConfig().rotateIntervalMillis();
        option.getConfig().executorService().scheduleAtFixedRate(new Runnable() {
            @Override public void run() {
                checkBeans(option, cacheKey);
            }
        }, intervalMillis, intervalMillis, MILLISECONDS);
    }

    @SneakyThrows
    protected int firstCheckBeans(final WestCacheOption option, String cacheKey) {
        val snapshot = option.getSnapshot();
        if (snapshot == null) return checkBeans(option, cacheKey);

        return futureGet(option, cacheKey);
    }

    @SneakyThrows
    private int futureGet(final WestCacheOption option,
                          final String cacheKey) {
        Future<Object> future = option.getConfig().executorService()
                .submit(new Callable<Object>() {
                    @Override public Object call() throws Exception {
                        return checkBeans(option, cacheKey);
                    }
                });

        long timeout = option.getConfig().timeoutMillisToSnapshot();
        try {
            future.get(timeout, MILLISECONDS);
        } catch (TimeoutException ex) {
            log.info("get first check beans {} timeout in " +
                    "{} millis, try snapshot", cacheKey, timeout);
            WestCacheItem result = option.getSnapshot().
                    readSnapshot(option, cacheKey + ".tableflushers");
            log.info("got {} snapshot {}", cacheKey,
                    result != null ? result.getObject() : " non-exist");
            if (result != null) return 1;
        }

        future.get();
        return 1;
    }

    @SneakyThrows
    protected int checkBeans(WestCacheOption option, String cacheKey) {
        log.debug("start rotating check");
        val beans = queryAllBeans();

        if (lastExecuted == -1) {
            tableRows = beans;
            saveSnapshot(option, cacheKey);
        } else if (beans.equals(tableRows)) {
            log.debug("no changes detected");
        } else {
            diff(tableRows, beans, option);
            tableRows = beans;
        }
        lastExecuted = System.currentTimeMillis();
        return 1;
    }

    private void saveSnapshot(WestCacheOption option, String cacheKey) {
        val snapshot = option.getSnapshot();
        if (snapshot == null) return;

        option.getSnapshot().saveSnapshot(option,
                cacheKey + ".tableflushers", new WestCacheItem(tableRows));
    }

    protected void diff(List<WestCacheFlusherBean> table,
                        List<WestCacheFlusherBean> beans,
                        WestCacheOption option) {
        val flushKeys = getDiffFlushKeys(table, beans);
        if (flushKeys.isEmpty()) return;

        Set<String> prefixKeys = Sets.newHashSet();
        Set<String> fullKeys = Sets.newHashSet();
        getFlushKeys(flushKeys, prefixKeys, fullKeys);

        log.debug("flush full keys:{}", fullKeys);
        log.debug("flush prefix keys:{}", prefixKeys);

        for (String fullKey : fullKeys) {
            flush(option, fullKey);
        }

        for (String prefixKey : prefixKeys) {
            flushPrefix(prefixKey);
        }
    }

    private Map<String, WestCacheFlusherBean> getDiffFlushKeys(
            List<WestCacheFlusherBean> table, List<WestCacheFlusherBean> beans) {
        Map<String, WestCacheFlusherBean> flushKeys = Maps.newHashMap();
        for (val bean : table) {
            val found = find(bean, beans);
            if (found == null || found.getValueVersion() != bean.getValueVersion()) {
                flushKeys.put(bean.getCacheKey(), bean);
            }
        }
        return flushKeys;
    }

    private void getFlushKeys(Map<String, WestCacheFlusherBean> flushKeys,
                              Set<String> prefixKeys, Set<String> fullKeys) {
        for (val key : getRegistry().asMap().keySet()) {
            if (flushKeys.containsKey(key)) {
                fullKeys.add(key);
                continue;
            }

            for (val bean : flushKeys.values()) {
                if (!"prefix".equals(bean.getKeyMatch())) continue;
                if (Keys.isPrefix(key, bean.getCacheKey())) {
                    fullKeys.add(key);
                    prefixKeys.add(bean.getCacheKey());
                }
            }
        }
    }

    protected void flushPrefix(String prefixKey) {
        prefixDirectCache.invalidate(prefixKey);
    }

    protected WestCacheFlusherBean find(WestCacheFlusherBean bean,
                                        List<WestCacheFlusherBean> beans) {
        for (val newbean : beans) {
            if (bean.getCacheKey().equals(newbean.getCacheKey()))
                return newbean;
        }
        return null;
    }
}