package com.github.bingoohuang.westcache.flusher;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.*;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
@Slf4j
public abstract class TableBasedCacheFlusher extends SimpleCacheFlusher {
    public static final String PREFIX = "prefix";
    public static final String REGEX = "regex";
    public static final String FULL = "full";

    volatile List<WestCacheFlusherBean> tableRows;
    volatile ScheduledFuture<?> scheduledFuture;

    @Getter volatile long lastExecuted = -1;
    Cache<String, Optional<Map<String, String>>> prefixDirectCache
            = CacheBuilder.newBuilder().build();
    private ScheduledExecutorService executorService
            = Executors.newSingleThreadScheduledExecutor();

    @Override
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

    public void cancelRotateChecker() {
        val future = scheduledFuture;
        scheduledFuture = null;

        if (future == null) return;
        if (future.isDone()) return;

        future.cancel(false);
        lastExecuted = -1;
    }

    @Override
    public Optional<Object> getDirectValue(
            WestCacheOption option, String cacheKey) {
        val bean = findBean(cacheKey);
        if (bean == null) return Optional.absent();

        Object value = null;
        if ("direct".equals(bean.getValueType())) {
            if ("full".equals(bean.getKeyMatch())) {
                value = readDirectValue(option, bean, DirectValueType.FULL);
            } else if (PREFIX.equals(bean.getKeyMatch())) {
                int subKeyStart = bean.getCacheKey().length() + 1;
                val subKey = cacheKey.substring(subKeyStart);
                value = readSubDirectValue(option, bean, subKey);
            }
        }

        return Optional.fromNullable(value);
    }

    protected abstract List<WestCacheFlusherBean> queryAllBeans();

    protected abstract Object readDirectValue(WestCacheOption option,
                                              WestCacheFlusherBean bean,
                                              DirectValueType type);

    @SuppressWarnings("unchecked")
    private <T> T readSubDirectValue(final WestCacheOption option,
                                     final WestCacheFlusherBean bean,
                                     String subKey) {
        val optional = Guavas.cacheGet(prefixDirectCache, bean.getCacheKey(), () -> {
            val map = readDirectValue(option, bean, DirectValueType.SUB);
            return Optional.fromNullable((Map<String, String>) map);
        });
        if (!optional.isPresent()) return null;

        val json = optional.get().get(subKey);
        if (json == null) return null;

        return FastJsons.parse(json, option.getMethod(), true);
    }

    protected WestCacheFlusherBean findBean(String cacheKey) {
        WestCacheFlusherBean bean;

        bean = findBeanByFullKey(cacheKey);
        if (bean != null) return bean;

        bean = findBeanByPrefix(cacheKey);
        if (bean != null) return bean;

        bean = findBeanByRegex(cacheKey);
        if (bean != null) return bean;

        return null;
    }

    private WestCacheFlusherBean findBeanByRegex(String cacheKey) {
        for (val bean : tableRows) {
            if (REGEX.equals(bean.getKeyMatch())
                    && Keys.matchRegex(cacheKey, bean.getCacheKey())) return bean;
        }

        return null;
    }

    private WestCacheFlusherBean findBeanByPrefix(String cacheKey) {
        for (val bean : tableRows) {
            if (PREFIX.equals(bean.getKeyMatch())
                    && Keys.isPrefix(cacheKey, bean.getCacheKey())) return bean;
        }

        return null;
    }

    private WestCacheFlusherBean findBeanByFullKey(String cacheKey) {
        for (val bean : tableRows) {
            if (FULL.equals(bean.getKeyMatch())
                    && bean.getCacheKey().equals(cacheKey)) return bean;
        }

        return null;
    }

    protected void startupRotateChecker(final WestCacheOption option,
                                        final String cacheKey) {
        firstCheckBeans(option, cacheKey);

        val intervalMillis = option.getConfig().rotateIntervalMillis();
        scheduledFuture = executorService.scheduleAtFixedRate(
                () -> checkBeans(option, cacheKey), intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    protected Object firstCheckBeans(final WestCacheOption option,
                                     String cacheKey) {
        val snapshot = option.getSnapshot();
        if (snapshot == null) return checkBeans(option, cacheKey);

        return futureGet(option, cacheKey);
    }

    private Object futureGet(final WestCacheOption option,
                             final String cacheKey) {

        String tableFlusherKey = cacheKey + ".tableflushers";
        return Envs.trySnapshot(option, executorService.submit((Callable<Object>) () -> checkBeans(option, cacheKey)), tableFlusherKey);
    }

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
                cacheKey + ".tableflushers",
                new WestCacheItem(Optional.fromNullable(tableRows), option));
    }

    protected void diff(List<WestCacheFlusherBean> table,
                        List<WestCacheFlusherBean> beans,
                        WestCacheOption option) {
        val flushKeys = getDiffFlushKeys(table, beans);
        if (flushKeys.isEmpty()) return;

        Map<String, String> prefixKeys = Maps.newHashMap();
        Map<String, String> fullKeys = Maps.newHashMap();
        getFlushKeys(flushKeys, prefixKeys, fullKeys);
        log.debug("flush full keys:{}, prefix keys:{}", fullKeys, prefixKeys);

        for (val entry : fullKeys.entrySet()) {
            flush(option, entry.getKey(), entry.getValue());
        }
        for (val entry : prefixKeys.entrySet()) {
            flushPrefix(entry.getKey());
        }
    }

    private Map<String, WestCacheFlusherBean> getDiffFlushKeys(
            List<WestCacheFlusherBean> table,
            List<WestCacheFlusherBean> beans) {
        Map<String, WestCacheFlusherBean> flushKeys = Maps.newHashMap();
        for (val bean : table) {
            val found = find(bean, beans);
            if (isBeanChanged(found, bean)) {
                flushKeys.put(bean.getCacheKey(), bean);
            }
        }
        return flushKeys;
    }

    private boolean isBeanChanged(WestCacheFlusherBean found,
                                  WestCacheFlusherBean old) {
        return found == null || found.getValueVersion() != old.getValueVersion();
    }

    private void getFlushKeys(Map<String, WestCacheFlusherBean> flushKeys,
                              Map<String, String> prefixKeys,
                              Map<String, String> fullKeys) {
        for (val key : getRegistry().asMap().keySet()) {
            if (flushKeys.containsKey(key)) {
                int valueVersion = flushKeys.get(key).getValueVersion();
                fullKeys.put(key, Integer.toString(valueVersion));
                continue;
            }

            for (val bean : flushKeys.values()) {
                if (!PREFIX.equals(bean.getKeyMatch())
                        || !Keys.isPrefix(key, bean.getCacheKey())) continue;

                fullKeys.put(key, "" + bean.getValueVersion());
                prefixKeys.put(bean.getCacheKey(), "" + bean.getValueVersion());
            }
        }
    }

    protected void flushPrefix(String prefixKey) {
        prefixDirectCache.invalidate(prefixKey);
    }

    protected WestCacheFlusherBean find(WestCacheFlusherBean old,
                                        List<WestCacheFlusherBean> beans) {
        for (val newOne : beans) {
            if (old.getCacheKey().equals(newOne.getCacheKey())) return newOne;
        }
        return null;
    }
}