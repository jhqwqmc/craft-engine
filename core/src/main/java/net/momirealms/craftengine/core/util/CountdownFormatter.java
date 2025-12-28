package net.momirealms.craftengine.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CountdownFormatter {
    private static final Pattern YEAR_PATTERN = Pattern.compile("[Yy]+");
    private static final Pattern MONTH_PATTERN = Pattern.compile("M+");
    private static final Pattern DAY_PATTERN = Pattern.compile("[Dd]+");
    private static final Pattern HOUR_PATTERN = Pattern.compile("[Hh]+");
    private static final Pattern MINUTE_PATTERN = Pattern.compile("m+");
    private static final Pattern SECOND_PATTERN = Pattern.compile("s+");
    private static final Pattern MILLIS_PATTERN = Pattern.compile("S+");
    private final String pattern;
    private final Matcher yearMatcher;
    private final Matcher monthMatcher;
    private final Matcher dayMatcher;
    private final Matcher hourMatcher;
    private final Matcher minuteMatcher;
    private final Matcher secondMatcher;
    private final Matcher millisMatcher;
    private final boolean hasYear;
    private final boolean hasMonth;
    private final boolean hasDay;
    private final boolean hasHour;
    private final boolean hasMinute;
    private final boolean hasSecond;
    private final boolean hasMillis;

    private CountdownFormatter(String pattern) {
        this.pattern = pattern;
        this.yearMatcher = YEAR_PATTERN.matcher(pattern);
        this.monthMatcher = MONTH_PATTERN.matcher(pattern);
        this.dayMatcher = DAY_PATTERN.matcher(pattern);
        this.hourMatcher = HOUR_PATTERN.matcher(pattern);
        this.minuteMatcher = MINUTE_PATTERN.matcher(pattern);
        this.secondMatcher = SECOND_PATTERN.matcher(pattern);
        this.millisMatcher = MILLIS_PATTERN.matcher(pattern);
        this.hasYear = yearMatcher.find();
        this.hasMonth = monthMatcher.find();
        this.hasDay = dayMatcher.find();
        this.hasHour = hourMatcher.find();
        this.hasMinute = minuteMatcher.find();
        this.hasSecond = secondMatcher.find();
        this.hasMillis = millisMatcher.find();
    }

    public static CountdownFormatter of(String pattern) {
        return new CountdownFormatter(pattern);
    }

    public String format(long millis) {
        long years = 0, months = 0, days = 0, hours = 0, minutes = 0, seconds = 0;

        if (!hasMillis) {
            seconds = millis / 1000;
            millis = 0;
        }
        if (!hasSecond) {
            minutes = seconds / 60;
            seconds = 0;
        }
        if (!hasMinute) {
            hours = minutes / 60;
            minutes = 0;
        }
        if (!hasHour) {
            days = hours / 24;
            hours = 0;
        }
        if (!hasDay) {
            months = days / 30;
            days = 0;
        }
        if (!hasMonth) {
            years = months / 12;
            months = 0;
        }

        if (hasMillis && hasSecond) {
            seconds = millis / 1000;
            millis %= 1000;
        }
        if (hasSecond && hasMinute) {
            minutes = seconds / 60;
            seconds %= 60;
        }
        if (hasMinute && hasHour) {
            hours = minutes / 60;
            minutes %= 60;
        }
        if (hasHour && hasDay) {
            days = hours / 24;
            hours %= 24;
        }
        if (hasDay && hasMonth) {
            months = days / 30;
            days %= 30;
        }
        if (hasMonth && hasYear) {
            years = months / 12;
            months %= 12;
        }

        StringBuilder result = new StringBuilder(pattern);
        replaceUnit(result, yearMatcher, years);
        replaceUnit(result, monthMatcher, months);
        replaceUnit(result, dayMatcher, days);
        replaceUnit(result, hourMatcher, hours);
        replaceUnit(result, minuteMatcher, minutes);
        replaceUnit(result, secondMatcher, seconds);
        replaceUnit(result, millisMatcher, millis);

        return result.toString();
    }

    private void replaceUnit(StringBuilder text, Matcher matcher, long value) {
        matcher.reset(text);
        if (matcher.find()) {
            int length = matcher.group().length();
            String formatted = String.format("%0" + length + "d", value);
            text.replace(matcher.start(), matcher.end(), formatted);
        }
    }
}
