package com.github.bingoohuang.westcache.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.util.Date;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/16.
 */
public class ScheduledParserTest {
    @Test
    public void cron() {
        Trigger trigger = new ScheduledParser("0 20 * * * ?").parse();
        assertThat(trigger.getStartTime()).isAtMost(new Date());
        assertThat(trigger.getEndTime()).isNull();
        assertThat(trigger).isInstanceOf(CronTrigger.class);
        CronTrigger cronTrigger = (CronTrigger) trigger;

        assertThat(cronTrigger.getCronExpression()).isEqualTo("0 20 * * * ?");
    }

    @Test
    public void at() {
        Trigger trigger = new ScheduledParser("At 01:20").parse();
        assertThat(trigger.getStartTime()).isAtMost(new Date());
        assertThat(trigger.getEndTime()).isNull();
        assertThat(trigger).isInstanceOf(CronTrigger.class);
        CronTrigger cronTrigger = (CronTrigger) trigger;

        assertThat(cronTrigger.getCronExpression()).isEqualTo("0 20 1 ? * *");
    }

    @Test
    public void atEvery() {
        Trigger trigger = new ScheduledParser("At ??:20").parse();
        assertThat(trigger.getStartTime()).isAtMost(new Date());
        assertThat(trigger.getEndTime()).isNull();
        assertThat(trigger).isInstanceOf(CronTrigger.class);
        CronTrigger cronTrigger = (CronTrigger) trigger;

        assertThat(cronTrigger.getCronExpression()).isEqualTo("0 20 * * * ?");
    }

    @Test
    public void everyMinutes() {
        Trigger trigger = new ScheduledParser("Every 20 minutes").parse();
        assertThat(trigger.getStartTime()).isAtMost(new Date());
        assertThat(trigger.getEndTime()).isNull();
        assertThat(trigger).isInstanceOf(SimpleTrigger.class);
        SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
        assertThat(simpleTrigger.getRepeatInterval()).isEqualTo(20L * 60 * 1000);
    }

    @Test
    public void everyMinutesFromTo() {
        Trigger trigger = new ScheduledParser("Every 20 minutes from 2016-10-10 to 2217-10-12").parse();
        assertThat(trigger.getStartTime()).isAtMost(new Date());
        assertThat(trigger.getEndTime()).isEqualTo(formatter.parseDateTime("2217-10-12 23:59:59").toDate());
        assertThat(trigger).isInstanceOf(SimpleTrigger.class);
        SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
        assertThat(simpleTrigger.getRepeatInterval()).isEqualTo(20L * 60 * 1000);
    }

    @Test
    public void everyMinutesFrom() {
        DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime tomorrow = DateTime.now().plusDays(1).withTimeAtStartOfDay();
        String tomorrowStr = tomorrow.toString(dayFormatter);


        Trigger trigger = new ScheduledParser("0 20 * * * ? from " + tomorrowStr).parse();
        assertThat(trigger.getStartTime()).isEqualTo(tomorrow.toDate());
        assertThat(trigger.getEndTime()).isNull();
        assertThat(trigger).isInstanceOf(CronTrigger.class);
        CronTrigger cronTrigger = (CronTrigger) trigger;

        assertThat(cronTrigger.getCronExpression()).isEqualTo("0 20 * * * ?");
    }

    static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    public void everyMinutesTo() {
        Trigger trigger = new ScheduledParser("At ??:20 to 2217-10-12").parse();
        assertThat(trigger.getStartTime()).isAtMost(new Date());
        assertThat(trigger.getEndTime()).isEqualTo(formatter.parseDateTime("2217-10-12 23:59:59").toDate());
        assertThat(trigger).isInstanceOf(CronTrigger.class);
        CronTrigger cronTrigger = (CronTrigger) trigger;

        assertThat(cronTrigger.getCronExpression()).isEqualTo("0 20 * * * ?");
    }

    @Test
    public void everySeconds() {
        Trigger trigger = new ScheduledParser("Every 20 seconds").parse();
        assertThat(trigger.getStartTime()).isAtMost(new Date());
        assertThat(trigger.getEndTime()).isNull();
        assertThat(trigger).isInstanceOf(SimpleTrigger.class);
        SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
        assertThat(simpleTrigger.getRepeatInterval()).isEqualTo(20L * 1000);
    }

    @Test
    public void everyHourse() {
        Trigger trigger = new ScheduledParser("Every 20 hours").parse();
        assertThat(trigger.getStartTime()).isAtMost(new Date());
        assertThat(trigger.getEndTime()).isNull();
        assertThat(trigger).isInstanceOf(SimpleTrigger.class);
        SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
        assertThat(simpleTrigger.getRepeatInterval()).isEqualTo(20L * 60 * 60 * 1000);
    }

    @Test(expected = RuntimeException.class)
    public void empty() {
        new ScheduledParser(null);
    }

    @Test(expected = RuntimeException.class)
    public void badFromTo() {
        new ScheduledParser("Every 20 minutes from 2018-10-10 to 2217-10-12").parse();
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
