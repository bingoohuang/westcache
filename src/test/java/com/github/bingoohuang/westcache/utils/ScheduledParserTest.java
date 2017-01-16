package com.github.bingoohuang.westcache.utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/16.
 */
public class ScheduledParserTest {
    @Test
    public void cron() {
        Scheduled scheduled = new ScheduledParser("0 20 * * * ?").parse();
        assertThat(scheduled.getFromDate()).isNull();
        assertThat(scheduled.getToDate()).isNull();
        assertThat(scheduled.getTrigger()).isInstanceOf(CronTrigger.class);
        CronTrigger cronTrigger = (CronTrigger) scheduled.getTrigger();

        assertThat(cronTrigger.getCronExpression()).isEqualTo("0 20 * * * ?");
    }

    @Test
    public void at() {
        Scheduled scheduled = new ScheduledParser("At 01:20").parse();
        assertThat(scheduled.getFromDate()).isNull();
        assertThat(scheduled.getToDate()).isNull();
        assertThat(scheduled.getTrigger()).isInstanceOf(CronTrigger.class);
        CronTrigger cronTrigger = (CronTrigger) scheduled.getTrigger();

        assertThat(cronTrigger.getCronExpression()).isEqualTo("0 20 1 ? * *");
    }

    @Test
    public void atEvery() {
        Scheduled scheduled = new ScheduledParser("At ??:20").parse();
        assertThat(scheduled.getFromDate()).isNull();
        assertThat(scheduled.getToDate()).isNull();
        assertThat(scheduled.getTrigger()).isInstanceOf(CronTrigger.class);
        CronTrigger cronTrigger = (CronTrigger) scheduled.getTrigger();

        assertThat(cronTrigger.getCronExpression()).isEqualTo("0 20 * * * ?");
    }

    @Test
    public void everyMinutes() {
        Scheduled scheduled = new ScheduledParser("Every 20 minutes").parse();
        assertThat(scheduled.getFromDate()).isNull();
        assertThat(scheduled.getToDate()).isNull();
        assertThat(scheduled.getTrigger()).isInstanceOf(SimpleTrigger.class);
        SimpleTrigger simpleTrigger = (SimpleTrigger) scheduled.getTrigger();
        assertThat(simpleTrigger.getRepeatInterval()).isEqualTo(20L * 60 * 1000);
    }

    @Test
    public void everyMinutesFromTo() {
        Scheduled scheduled = new ScheduledParser("Every 20 minutes from 2016-10-10 to 2017-10-12").parse();
        assertThat(scheduled.getFromDate()).isEqualTo(formatter.parseDateTime("2016-10-10 00:00:00"));
        assertThat(scheduled.getToDate()).isEqualTo(formatter.parseDateTime("2017-10-12 23:59:59"));
        assertThat(scheduled.getTrigger()).isInstanceOf(SimpleTrigger.class);
        SimpleTrigger simpleTrigger = (SimpleTrigger) scheduled.getTrigger();
        assertThat(simpleTrigger.getRepeatInterval()).isEqualTo(20L * 60 * 1000);
    }

    @Test
    public void everyMinutesFrom() {
        Scheduled scheduled = new ScheduledParser("0 20 * * * ? from 2016-10-10").parse();
        assertThat(scheduled.getFromDate()).isEqualTo(formatter.parseDateTime("2016-10-10 00:00:00"));
        assertThat(scheduled.getToDate()).isNull();
        assertThat(scheduled.getTrigger()).isInstanceOf(CronTrigger.class);
        CronTrigger cronTrigger = (CronTrigger) scheduled.getTrigger();

        assertThat(cronTrigger.getCronExpression()).isEqualTo("0 20 * * * ?");
    }

    static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    public void everyMinutesTo() {
        Scheduled scheduled = new ScheduledParser("At ??:20 to 2017-10-12").parse();
        assertThat(scheduled.getFromDate()).isNull();
        assertThat(scheduled.getToDate()).isEqualTo(formatter.parseDateTime("2017-10-12 23:59:59"));
        assertThat(scheduled.getTrigger()).isInstanceOf(CronTrigger.class);
        CronTrigger cronTrigger = (CronTrigger) scheduled.getTrigger();

        assertThat(cronTrigger.getCronExpression()).isEqualTo("0 20 * * * ?");
    }

    @Test
    public void everySeconds() {
        Scheduled scheduled = new ScheduledParser("Every 20 seconds").parse();
        assertThat(scheduled.getFromDate()).isNull();
        assertThat(scheduled.getToDate()).isNull();
        assertThat(scheduled.getTrigger()).isInstanceOf(SimpleTrigger.class);
        SimpleTrigger simpleTrigger = (SimpleTrigger) scheduled.getTrigger();
        assertThat(simpleTrigger.getRepeatInterval()).isEqualTo(20L * 1000);
    }

    @Test
    public void everyHourse() {
        Scheduled scheduled = new ScheduledParser("Every 20 hours").parse();
        assertThat(scheduled.getFromDate()).isNull();
        assertThat(scheduled.getToDate()).isNull();
        assertThat(scheduled.getTrigger()).isInstanceOf(SimpleTrigger.class);
        SimpleTrigger simpleTrigger = (SimpleTrigger) scheduled.getTrigger();
        assertThat(simpleTrigger.getRepeatInterval()).isEqualTo(20L * 60 * 60 * 1000);
    }

    @Test(expected = RuntimeException.class)
    public void empty() {
        new ScheduledParser(null);
    }

    @Test(expected = RuntimeException.class)
    public void badFromTo() {
        new ScheduledParser("Every 20 minutes from 2018-10-10 to 2017-10-12").parse();
    }

    @Test(expected = RuntimeException.class)
    public void badAt() {
        new ScheduledParser("At 1234").parse();
    }

    @Test(expected = RuntimeException.class)
    public void badEvery() {
        new ScheduledParser("Every 1234").parse();
    }
}
