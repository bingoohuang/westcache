package com.github.bingoohuang.westcache.base;

import com.google.common.base.Optional;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/29.
 */
public class WestCacheItem {
    private final Optional<Object> optional;

    public WestCacheItem(Object object) {
        this(Optional.fromNullable(object));
    }

    public WestCacheItem(Optional<Object> object) {
        this.optional = object;
    }

    public Optional<Object> getObject() {
        return optional;
    }

    public Object orNull() {
        Optional<Object> object = getObject();
        return object == null ? null : object.orNull();
    }

    public boolean isPresent() {
        return optional.isPresent();
    }
}
