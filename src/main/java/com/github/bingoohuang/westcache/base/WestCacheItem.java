package com.github.bingoohuang.westcache.base;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.val;

import static com.github.bingoohuang.westcache.utils.Durations.parse;
import static com.github.bingoohuang.westcache.utils.ExpireAfterWrites.parseExpireAfterWrite;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/29.
 */
public class WestCacheItem {
    private final Optional optional;
    @Getter private final long durationSeconds;
    private final long expiredAt;

    public WestCacheItem(Optional optional, WestCacheOption option) {
        this.optional = optional;
        val expireWrite = parseExpireAfterWrite(option, optional.orNull());
        if (isBlank(expireWrite)) {
            this.durationSeconds = 0;
            this.expiredAt = 0;
            return;
        }

        val duration = parse("expireAfterWrite", expireWrite);
        this.durationSeconds = duration;
        this.expiredAt = System.currentTimeMillis() + duration * 1000;
    }

    public Optional getObject() {
        return optional;
    }

    public Object orNull() {
        return optional == null ? null : optional.orNull();
    }

    public boolean isPresent() {
        return optional.isPresent();
    }

    public boolean isExpiredAfterWrite() {
        return expiredAt > 0 && System.currentTimeMillis() > expiredAt;
    }


}
