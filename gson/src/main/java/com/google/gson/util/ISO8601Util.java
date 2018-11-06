package com.google.gson.util;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

/**
 * Utility class to assist with parsing dates using the ISO 8601 standard format.
 *
 */
public class ISO8601Util {

    private static final ThreadLocal<DateTimeFormatter> threadLocalDateTimeFormatter =
            new ThreadLocal<DateTimeFormatter>() {
                @Override public DateTimeFormatter initialValue() {
                    return new DateTimeFormatterBuilder().appendPattern(
                            "yyyy-MM-dd'T'HH:mm:ss").parseLenient().optionalStart().appendPattern(".SSS").optionalEnd()
                            .optionalStart().appendOffset("+HH:MM", "Z").optionalEnd().toFormatter();
                    }
            };

    private static final ThreadLocal<DateTimeFormatter> threadLocalDateFormatter =
            new ThreadLocal<DateTimeFormatter>() {
                @Override public DateTimeFormatter initialValue() {
                    return DateTimeFormatter.ISO_DATE;
                }
            };

    private static final ThreadLocal<DateTimeFormatter> threadLocalTimeFormatter =
            new ThreadLocal<DateTimeFormatter>() {
                @Override public DateTimeFormatter initialValue() {
                    return DateTimeFormatter.ISO_DATE;
                }
            };

    /**
     * Transform the <code>LocalDate</code> instance into an ISO 8601 string.
     *
     * @param localDate the <code>LocalDate</code> instance to transform into an ISO 8601 string
     *
     * @return the ISO 8601 string for the <code>LocalDate</code> instance
     */
    public static String fromLocalDate(LocalDate localDate)
    {
        return localDate.format(threadLocalDateFormatter.get());
    }

    /**
     * Transform the <code>LocalDateTime</code> instance into an ISO 8601 string.
     *
     * @param localDateTime the <code>LocalDateTime</code> instance to transform into an ISO 8601 string
     *
     * @return the ISO 8601 string for the <code>LocalDateTime</code> instance
     */
    public static String fromLocalDateTime(LocalDateTime localDateTime)
    {
        return localDateTime.format(threadLocalDateTimeFormatter.get());
    }

    /**
     * Transform the <code>LocalTime</code> instance into an ISO 8601 string.
     *
     * @param localTime the <code>LocalTime</code> instance to transform into an ISO 8601 string
     *
     * @return the ISO 8601 string for the <code>LocalTime</code> instance
     */
    public static String fromLocalTime(LocalTime localTime)
    {
        return localTime.format(threadLocalTimeFormatter.get());
    }

    /**
     * Transform the <code>ZonedDateTime</code> instance into an ISO 8601 string.
     *
     * @param zonedDateTime the <code>ZonedDateTime</code> instance to transform into an ISO 8601
     *                      string
     *
     * @return the ISO 8601 string for the <code>ZonedDateTime</code> instance
     */
    public static String fromZonedDateTime(ZonedDateTime zonedDateTime)
    {
        return zonedDateTime.format(threadLocalDateTimeFormatter.get());
    }

    /**
     * Get current date and time formatted as ISO 8601 string.
     *
     * @return the current date and time formatted as ISO 8601 string
     */
    public static String now()
    {
        return fromLocalDateTime(LocalDateTime.now());
    }

    /**
     * Transform ISO 8601 string into a <code>LocalDate</code> instance.
     *
     * @param iso8601string the ISO 8601 string to transform
     *
     * @return the <code>LocalDate</code> instance for the ISO 8601 string
     *
     * @throws ParseException
     */
    public static LocalDate toLocalDate(String iso8601string)
            throws ParseException
    {
        return LocalDate.parse(iso8601string, threadLocalDateFormatter.get());
    }

    /**
     * Transform ISO 8601 string into a <code>LocalDateTime</code> instance.
     *
     * @param iso8601string the ISO 8601 string to transform
     *
     * @return the <code>LocalDateTime</code> instance for the ISO 8601 string
     *
     * @throws ParseException
     */
    public static LocalDateTime toLocalDateTime(String iso8601string)
            throws ParseException
    {
        TemporalAccessor temporalAccessor = threadLocalDateTimeFormatter.get().parse(iso8601string);

        if (temporalAccessor.isSupported(ChronoField.OFFSET_SECONDS))
        {
            return ZonedDateTime.parse(iso8601string, threadLocalDateTimeFormatter.get())
                    .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }
        else
        {
            return LocalDateTime.parse(iso8601string, threadLocalDateTimeFormatter.get());
        }
    }

    /**
     * Transform ISO 8601 string into a <code>LocalTime</code> instance.
     *
     * @param iso8601string the ISO 8601 string to transform
     *
     * @return the <code>LocalTime</code> instance for the ISO 8601 string
     *
     * @throws ParseException
     */
    public static LocalTime toLocalTime(String iso8601string)
            throws ParseException
    {
        return LocalTime.parse(iso8601string, threadLocalTimeFormatter.get());
    }

    /**
     * Transform ISO 8601 string into a <code>ZonedDateTime</code> instance.
     *
     * @param iso8601string the ISO 8601 string to transform
     *
     * @return the <code>ZonedDateTime</code> instance for the ISO 8601 string
     *
     * @throws ParseException
     */
    public static ZonedDateTime toZonedDateTime(String iso8601string)
            throws ParseException
    {
        TemporalAccessor temporalAccessor = threadLocalDateTimeFormatter.get().parse(iso8601string);

        if (temporalAccessor.isSupported(ChronoField.OFFSET_SECONDS))
        {
            return ZonedDateTime.parse(iso8601string, threadLocalDateTimeFormatter.get()).withZoneSameInstant(ZoneId.systemDefault());
        }
        else
        {
            return LocalDateTime.parse(iso8601string, threadLocalDateTimeFormatter.get()).atZone(ZoneId.systemDefault());
        }
    }
}