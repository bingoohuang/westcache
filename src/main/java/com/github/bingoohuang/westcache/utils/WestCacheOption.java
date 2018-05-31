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
import lombok.val;

import java.lang.reflect.Method;
import java.util.Map;

import static com.github.bingoohuang.westcache.WestCacheRegistry.*;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@AllArgsConstructor
public class WestCacheOption {
    public static final String FLUSHER_NAME = "flusher";
    public static final String MANAGER_NAME = "manager";
    public static final String CONFIG_NAME = "config";
    public static final String INTERCEPTOR_NAME = "interceptor";
    public static final String KEYER_NAME = "keyer";

    @Getter private final WestCacheFlusher flusher;
    @Getter private final WestCacheManager manager;
    @Getter private final WestCacheSnapshot snapshot;
    @Getter private final WestCacheConfig config;
    @Getter private final WestCacheInterceptor interceptor;
    @Getter private final WestCacheKeyer keyer;
    @Getter private final String key;
    @Getter private final Map<String, String> specs;
    @Getter private final Method method;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        WestCacheFlusher flusher = FLUSHER_REGISTRY.get("");
        WestCacheManager manager = MANAGER_REGISTRY.get("");
        WestCacheSnapshot snapshot = SNAPSHOT_REGISTRY.get("");
        WestCacheConfig config = REGISTRY_TEMPLATE.get("");
        WestCacheInterceptor interceptor = INTERCEPTOR_REGISTRY.get("");
        WestCacheKeyer keyer = KEYER_REGISTRY.get("");
        String key = "";
        Map<String, String> specs = Maps.newHashMap();
        Method method;

        public Builder flusher(String flusherName) {
            this.flusher = FLUSHER_REGISTRY.get(flusherName);
            checkNotNull(this.flusher, flusherName, FLUSHER_NAME);

            return this;
        }

        public Builder manager(String managerName) {
            this.manager = MANAGER_REGISTRY.get(managerName);
            checkNotNull(this.manager, managerName, MANAGER_NAME);

            return this;
        }

        public Builder snapshot(String snapshotName) {
            this.snapshot = SNAPSHOT_REGISTRY.get(snapshotName);

            return this;
        }

        public Builder config(String configName) {
            this.config = REGISTRY_TEMPLATE.get(configName);
            checkNotNull(this.config, configName, CONFIG_NAME);

            return this;
        }

        public Builder interceptor(String interceptorName) {
            this.interceptor = INTERCEPTOR_REGISTRY.get(interceptorName);
            checkNotNull(this.interceptor, interceptorName, INTERCEPTOR_NAME);

            return this;
        }

        public Builder keyer(String keyerName) {
            this.keyer = KEYER_REGISTRY.get(keyerName);
            checkNotNull(this.keyer, keyerName, KEYER_NAME);

            return this;
        }

        private void checkNotNull(Object object, String name, String attr) {
            if (object != null) return;

            val newName = name.isEmpty() ? "default" : name;
            throw new WestCacheException(attr + " " + newName + " is not registered, " +
                            "please check your config or dependencies");
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

        public Builder clone(WestCacheOption option) {
            this.flusher = option.flusher;
            this.manager = option.manager;
            this.snapshot = option.snapshot;
            this.config = option.config;
            this.interceptor = option.interceptor;
            this.keyer = option.keyer;
            this.key = option.key;
            this.specs = option.specs;
            this.method = option.method;
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
                public Optional<WestCacheOption> load(Method method) {
                    val attrs = Anns.parseWestCacheable(method, WestCacheable.class);
                    if (attrs == null) return Optional.absent();

                    val annAttributes = attrs.getAnnotationAttributes();
                    val opt = buildOption(annAttributes, attrs.getMethod());
                    return Optional.of(opt);
                }
            });

    public static WestCacheOption parseWestCacheable(Method m) {
        return Guavas.cacheGet(optionCache, m).orNull();
    }

    private static WestCacheOption buildOption(Map<String, String> attrs, Method m) {
        return WestCacheOption.builder()
                .flusher(getAttr(attrs, FLUSHER_NAME))
                .manager(getAttr(attrs, MANAGER_NAME))
                .snapshot(getAttr(attrs, "snapshot"))
                .config(getAttr(attrs, CONFIG_NAME))
                .interceptor(getAttr(attrs, INTERCEPTOR_NAME))
                .keyer(getAttr(attrs, KEYER_NAME))
                .key(getAttr(attrs, "key"))
                .specs(parseSpecs(attrs))
                .method(m)
                .build();
    }

    private static Map<String, String> parseSpecs(Map<String, String> attrs) {
        val specsStr = attrs.get("specs");
        val specs = Specs.parseSpecs(specsStr);

        Anns.removeAttrs(attrs, FLUSHER_NAME, MANAGER_NAME,
                "snapshot", CONFIG_NAME, INTERCEPTOR_NAME, KEYER_NAME,
                "key", "specs");
        specs.putAll(attrs);
        return specs;
    }

    private static String getAttr(Map<String, String> attrs, String attrName) {
        String attr = attrs.get(attrName);
        return attr == null ? "" : attr;
    }
}
