package net.protsenko.util;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import static java.time.temporal.ChronoField.*;

public class DateFormatterUtil {
    public final static DateTimeFormatter fullDateTimeFormatter = new DateTimeFormatterBuilder()
            .appendValue(YEAR_OF_ERA, 4)
            .appendLiteral('-')
            .appendValue(MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(DAY_OF_MONTH, 2)
            .appendLiteral(' ')
            .appendValue(HOUR_OF_DAY)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE)
            .toFormatter();

    public final static DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE)
            .toFormatter();
}
