package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.WestCacheable;
import com.github.bingoohuang.westcache.base.*;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import lombok.val;

import java.lang.reflect.Method;
import java.util.Map;

import static com.github.bingoohuang.westcache.WestCacheRegistry.*;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@Value @AllArgsConstructor
public class WestCacheOption {
    @Getter private final WestCacheFlusher flusher;
    @Getter private final WestCacheManager manager;
    @Getter private final WestCacheSnapshot snapshot;
    @Getter private final WestCacheConfig config;
    @Getter private final WestCacheInterceptor interceptor;
    @Getter private final WestCacheKeyer keyer;
    @Getter private final String key;
    @Getter private final Map<String, String> specs;
    @Getter private final Method method;

    public static Builder newBuilder() {
        return new Builder();
    }


    public static class Builder {
        WestCacheFlusher flusher = flusherRegistry.get("");
        WestCacheManager manager = managerRegistry.get("");
        WestCacheSnapshot snapshot = snapshotRegistry.get("");
        WestCacheConfig config = configRegistry.get("");
        WestCacheInterceptor interceptor = interceptorRegistry.get("");
        WestCacheKeyer keyer = keyerRegistry.get("");
        String key = "";
        Map<String, String> specs = Maps.newHashMap();
        Method method;

        public Builder flusher(String flusherName) {
            this.flusher = flusherRegistry.get(flusherName);
            return this;
        }

        public Builder manager(String managerName) {
            this.manager = managerRegistry.get(managerName);
            return this;
        }

        public Builder snapshot(String snapshotName) {
            this.snapshot = snapshotRegistry.get(snapshotName);
            return this;
        }

        public Builder config(String configName) {
            this.config = configRegistry.get(configName);
            return this;
        }

        public Builder interceptor(String interceptorName) {
            this.interceptor = interceptorRegistry.get(interceptorName);
            return this;
        }

        public Builder keyer(String keyerName) {
            this.keyer = keyerRegistry.get(keyerName);
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder specs(String specs) {
            this.specs = Specs.parseSpecs(specs);
            return this;
        }

        public Builder specs(Map<String, String> specs) {
            this.specs = specs;
            return this;
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public WestCacheOption build() {
            return new WestCacheOption(flusher, manager, snapshot,
                    config, interceptor, keyer, key, specs, method);
        }
    }

    static LoadingCache<Method, Optional<WestCacheOption>> optionCache
            = CacheBuilder.newBuilder().build(
            new CacheLoader<Method, Optional<WestCacheOption>>() {
                @Override
                public Optional<WestCacheOption> load(Method m) throws Exception {
                    val attrs = Anns.parseWestCacheable(m, WestCacheable.class);
                    val opt = attrs == null ? null : buildOption(attrs, m);
                    return Optional.fromNullable(opt);
                }
            });

    public static WestCacheOption parseWestCacheable(Method m) {
        return optionCache.getUnchecked(m).orNull();
    }

    private static WestCacheOption buildOption(
            Map<String, String> attrs, Method m) {
        return WestCacheOption.newBuilder()
                .flusher(getAttr(attrs, "flusher"))
                .manager(getAttr(attrs, "manager"))
                .snapshot(getAttr(attrs, "snapshot"))
                .config(getAttr(attrs, "config"))
                .interceptor(getAttr(attrs, "interceptor"))
                .keyer(getAttr(attrs, "keyer"))
                .key(getAttr(attrs, "key"))
                .specs(parseSpecs(attrs))
                .method(m)
                .build();
    }

    private static Map<String, String> parseSpecs(Map<String, String> attrs) {
        String specsStr = attrs.get("specs");
        val specs = Specs.parseSpecs(specsStr);

        Anns.removeAttrs(attrs, "flusher", "manager",
                "snapshot", "config", "interceptor",
                "keyer", "key", "specs");
        specs.putAll(attrs);
        return specs;
    }

    private static String getAttr(Map<String, String> attrs,
                                  String attrName) {
        String attr = attrs.get(attrName);
        return attr == null ? "" : attr;
    }
}
