package com.github.bingoohuang.westcache.base;

import com.google.common.base.Optional;
import lombok.Getter;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/29.
 */
public class WestCacheItem {
    @Getter private final long createTime;
    private final Optional<Object> optional;
    @Getter private long shotTimes;

    public WestCacheItem(Object object) {
        this(Optional.fromNullable(object));
    }

    public WestCacheItem(Optional<Object> object) {
        this.optional = object;
        this.createTime = System.currentTimeMillis();
    }

    public Optional<Object> getObject() {
        ++shotTimes;
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
