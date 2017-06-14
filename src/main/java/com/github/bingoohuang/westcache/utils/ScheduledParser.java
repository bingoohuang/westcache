package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.base.WestCacheException;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.substring;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/16.
 */
public class ScheduledParser {
    private static final Pattern FROM_PATTERN = Pattern.compile(
            "\\bfrom\\b\\s*(\\d\\d\\d\\d-\\d\\d-\\d\\d)( \\d\\d:\\d\\d:\\d\\d)?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TO_PATTERN = Pattern.compile(
            "\\bto\\b\\s*(\\d\\d\\d\\d-\\d\\d-\\d\\d)( \\d\\d:\\d\\d:\\d\\d)?",
            Pattern.CASE_INSENSITIVE);
    private String schedulerExpr;


    public ScheduledParser(String schedulerExpr) {
        this.schedulerExpr = StringUtils.trim(schedulerExpr);
        if (StringUtils.isBlank(this.schedulerExpr))
            throw new WestCacheException("scheduler expression can not be blank");
    }

    /**
     * Parse scheduled expression like followings:
     * Every 1 minute 每1分钟
     * Every 30 minutes 每30分钟
     * Every 10 hours 每10小时
     * Every 60 seconds 每60秒
     * At 03:00 每天凌晨3点
     * At ??:40 每小时的第40分钟
     * 0 20 * * * ? 每小时开始20分钟
     * Every 30 minutes from 2016-10-10 to 2017-10-12
     * At 03:00 to 2013-11-01
     * 0 20 * * * ? from 2013-10-10 14:00:00
     *
     * @return Scheduled parsed result.
     */
    public Trigger parse() {
        DateTime fromDate = parseDate(FROM_PATTERN, "00:00:00");
        DateTime toDate = parseDate(TO_PATTERN, "23:59:59");
        if (fromDate != null && toDate != null && fromDate.isAfterNow()) {
            throw new WestCacheException("scheduler expression is not valid " +
                    "because of from-date is after of to-date");
        }

        val scheduleBuilder = createScheduleBuilder();
        val triggerBuilder = newTrigger().withSchedule(scheduleBuilder);
        if (fromDate != null && fromDate.isAfterNow()) {
            triggerBuilder.startAt(fromDate.toDate());
        }
        if (toDate != null && toDate.isAfterNow()) {
            triggerBuilder.endAt(toDate.toDate());
        }

        return triggerBuilder.build();
    }

    private ScheduleBuilder<? extends Trigger> createScheduleBuilder() {
        if (StringUtils.startsWithIgnoreCase(schedulerExpr, "Every")) {
            return parseEveryExpr(schedulerExpr.substring("Every".length()));
        } else if (StringUtils.startsWithIgnoreCase(schedulerExpr, "At")) {
            return parseAtExpr(schedulerExpr.substring("At".length()));
        } else {
            return parseCron(schedulerExpr);
        }
    }

    private DateTime parseDate(Pattern pattern, String defaultTime) {
        Matcher matcher = pattern.matcher(schedulerExpr);
        if (!matcher.find())
            return null;

        schedulerExpr = removeFound(schedulerExpr, matcher);

        String fromDay = matcher.group(1);
        String timePart = matcher.group(2);
        String fromTime = timePart == null ? defaultTime : timePart.trim();

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        return formatter.parseDateTime(fromDay + " " + fromTime);
    }

    private String removeFound(String str, Matcher matcher) {
        int start = matcher.start();
        int end = matcher.end();

        String removed = substring(str, 0, start) + substring(str, end);
        return StringUtils.trim(removed);
    }

    private ScheduleBuilder<? extends Trigger> parseCron(String schedulerExpr) {
        return cronSchedule(schedulerExpr);
    }

    private static final Pattern AT_EXPR_PATTERN = Pattern.compile(
            "\\s+(\\d\\d|\\?\\?):(\\d\\d)", Pattern.CASE_INSENSITIVE);

    private ScheduleBuilder<? extends Trigger> parseAtExpr(String atExpr) {
        Matcher matcher = AT_EXPR_PATTERN.matcher(atExpr);
        if (!matcher.find()) {
            throwException(atExpr);
        }

        if (matcher.group(1).equals("??")) {
            String cronExpression = "0 " + matcher.group(2) + " * * * ?";
            return parseCron(cronExpression);
        }

        val formatter = DateTimeFormat.forPattern("HH:mm");
        val dateTime = formatter.parseDateTime(matcher.group().trim());

        int hourOfDay = dateTime.getHourOfDay();
        int minuteOfHour = dateTime.getMinuteOfHour();

        return dailyAtHourAndMinute(hourOfDay, minuteOfHour);
    }

    private static final Pattern EVERY_EXPR_PATTERN = Pattern.compile(
            "\\s+(\\d+)\\s*(h|hour|m|minute|s|second)s?", Pattern.CASE_INSENSITIVE);

    private ScheduleBuilder<? extends Trigger> parseEveryExpr(String everyExpr) {
        Matcher matcher = EVERY_EXPR_PATTERN.matcher(everyExpr);
        if (!matcher.find())
            return throwException(everyExpr);

        int num = Integer.parseInt(matcher.group(1));
        if (num <= 0)
            throwException(everyExpr);

        char unit = matcher.group(2).charAt(0);
        TimeUnit timeUnit = parseTimeUnit(unit);

        return simpleSchedule()
                .withIntervalInSeconds((int) timeUnit.toSeconds(num))
                .repeatForever();
    }

    private ScheduleBuilder<? extends Trigger> throwException(String everyExpr) {
        throw new WestCacheException(everyExpr + " is not valid");
    }

    private TimeUnit parseTimeUnit(char unit) {
        switch (unit) {
            case 'h':
            case 'H':
                return TimeUnit.HOURS;
            case 'm':
            case 'M':
                return TimeUnit.MINUTES;
            case 's':
            case 'S':
            default:
                return TimeUnit.SECONDS;
        }
    }
}
