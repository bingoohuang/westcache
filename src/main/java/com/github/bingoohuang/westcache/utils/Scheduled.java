package com.github.bingoohuang.westcache.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.quartz.Trigger;


/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/16.
 */
@AllArgsConstructor
public class Scheduled {
    @Getter final private DateTime fromDate;
    @Getter final private DateTime toDate;
    @Getter final private Trigger trigger;


    public boolean isToDateInFuture() {
        return toDate == null || toDate.isAfterNow();
    }

    public String getToDateStr() {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-ddHH:mm:ss");
        return toDate.toString(formatter);
    }
}
