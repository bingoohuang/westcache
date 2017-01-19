package com.github.bingoohuang.westcache.base;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/19.
 */
public interface ExpireAfterWritable {
    /**
     * Duration represented by an integer,
     * followed by one of "d", "h", "m", or "s",
     * representing days, hours, minutes, or seconds respectively.
     *
     * @return duration
     */
    String expireAfterWrite();
}
