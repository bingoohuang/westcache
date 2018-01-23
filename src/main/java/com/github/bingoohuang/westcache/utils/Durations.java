package com.github.bingoohuang.westcache.utils;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/2.
 */
@UtilityClass
public class Durations {
    public static long parse(String key, String spec) {
        checkArgument(spec != null && !spec.isEmpty(),
                "value of key %s omitted", key);
        try {
            char lastChar = spec.charAt(spec.length() - 1);
            val timeUnit = parseTimeUnit(key, spec, lastChar);

            val duration = Long.parseLong(spec.substring(0, spec.length() - 1));
            return TimeUnit.SECONDS.convert(duration, timeUnit);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    format("key %s value set to %s, must be integer", key, spec));
        }
    }

    private static TimeUnit parseTimeUnit(String key, String spec, char lastChar) {
        switch (lastChar) {
            case 'd':
                return TimeUnit.DAYS;
            case 'h':
                return TimeUnit.HOURS;
            case 'm':
                return TimeUnit.MINUTES;
            case 's':
                return TimeUnit.SECONDS;
            default:
                throw new IllegalArgumentException(
                        format("key %s invalid format.  was %s, " +
                                "must end with one of [dDhHmMsS]", key, spec));
        }
    }
}
