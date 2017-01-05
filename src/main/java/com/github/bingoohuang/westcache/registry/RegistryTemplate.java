package com.github.bingoohuang.westcache.registry;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/24.
 */
public class RegistryTemplate<T> {
    Cache<String, T> registry = CacheBuilder.newBuilder().build();

    public void register(String name, T object) {
        T cached = registry.getIfPresent(name);
        if (cached != null) throw new RuntimeException(
                "registry name " + name + " already exists");

        registry.put(name, object);
    }

    public void deregister(String name) {
        registry.invalidate(name);
    }

    public T get(String name) {
        return registry.getIfPresent(name.isEmpty() ? "default" : name);
    }
}
