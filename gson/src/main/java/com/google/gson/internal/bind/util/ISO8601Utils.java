package com.google.gson.internal.bind.util;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.*;

/**
 * Utilities methods for manipulating dates in iso8601 format. This is much much faster and GC friendly than using SimpleDateFormat so
 * highly suitable if you (un)serialize lots of date objects.
 * 
 * Supported parse format: [yyyy-MM-dd|yyyyMMdd][T(hh:mm[:ss[.sss]]|hhmm[ss[.sss]])]?[Z|[+-]hh[:]mm]]
 * 
 * @see <a href="http://www.w3.org/TR/NOTE-datetime">this specification</a>
 */
//Date parsing code from Jackson databind ISO8601Utils.java
// https://github.com/FasterXML/jackson-databind/blob/master/src/main/java/com/fasterxml/jackson/databind/util/ISO8601Utils.java
public class ISO8601Utils
{
    /**
     * ID to represent the 'UTC' string, default timezone since Jackson 2.7
     * 
     * @since 2.7
     */
    private static final String UTC_ID = "UTC";
    /**
     * The UTC timezone, prefetched to avoid more lookups.
     * 
     * @since 2.7
     */
    private static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone(UTC_ID);

    /*
    /**********************************************************
    /* Formatting
    /**********************************************************
     */

    /**
     * Format a date into 'yyyy-MM-ddThh:mm:ssZ' (default timezone, no milliseconds precision)
     * 
     * @param date the date to format
     * @return the date formatted as 'yyyy-MM-ddThh:mm:ssZ'
     */
    public static String format(Date date) {
        return format(date, false, TIMEZONE_UTC);
    }

    /**
     * Format a date into 'yyyy-MM-ddThh:mm:ss[.sss]Z' (GMT timezone)
     * 
     * @param date the date to format
     * @param millis true to include millis precision otherwise false
     * @return the date formatted as 'yyyy-MM-ddThh:mm:ss[.sss]Z'
     */
    public static String format(Date date, boolean millis) {
        return format(date, millis, TIMEZONE_UTC);
    }

    /**
     * Format date into yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm]
     * 
     * @param date the date to format
     * @param millis true to include millis precision otherwise false
     * @param tz timezone to use for the formatting (UTC will produce 'Z')
     * @return the date formatted as yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm]
     */
    public static String format(Date date, boolean millis, TimeZone tz) {
        Calendar calendar = new GregorianCalendar(tz, Locale.US);
        calendar.setTime(date);

        // estimate capacity of buffer as close as we can (yeah, that's pedantic ;)
        int capacity = "yyyy-MM-ddThh:mm:ss".length();
        capacity += millis ? ".sss".length() : 0;
        capacity += tz.getRawOffset() == 0 ? "Z".length() : "+hh:mm".length();
        StringBuilder formatted = new StringBuilder(capacity);

        padInt(formatted, calendar.get(Calendar.YEAR), "yyyy".length());
        formatted.append('-');
        padInt(formatted, calendar.get(Calendar.MONTH) + 1, "MM".length());
        formatted.append('-');
        padInt(formatted, calendar.get(Calendar.DAY_OF_MONTH), "dd".length());
        formatted.append('T');
        padInt(formatted, calendar.get(Calendar.HOUR_OF_DAY), "hh".length());
        formatted.append(':');
        padInt(formatted, calendar.get(Calendar.MINUTE), "mm".length());
        formatted.append(':');
        padInt(formatted, calendar.get(Calendar.SECOND), "ss".length());
        if (millis) {
            formatted.append('.');
            padInt(formatted, calendar.get(Calendar.MILLISECOND), "sss".length());
        }

        int offset = tz.getOffset(calendar.getTimeInMillis());
        if (offset != 0) {
            int hours = Math.abs((offset / (60 * 1000)) / 60);
            int minutes = Math.abs((offset / (60 * 1000)) % 60);
            formatted.append(offset < 0 ? '-' : '+');
            padInt(formatted, hours, "hh".length());
            formatted.append(':');
            padInt(formatted, minutes, "mm".length());
        } else {
            formatted.append('Z');
        }

        return formatted.toString();
    }

    /*
    /**********************************************************
    /* Parsing
    /**********************************************************
     */

    /**
     * Parse a date from ISO-8601 formatted string. It expects a format
     * [yyyy-MM-dd|yyyyMMdd][T(hh:mm[:ss[.sss]]|hhmm[ss[.sss]])]?[Z|[+-]hh[:mm]]]
     * 
     * @param date ISO string to parse in the appropriate format.
     * @param pos The position to start parsing from, updated to where parsing stopped.
     * @return the parsed date
     * @throws ParseException if the date is not in the appropriate format
     */
    public static Date parse(String date, ParsePosition pos) throws ParseException {
        Exception fail = null;
        try {
            int offset = pos.getIndex();

            // extract year
            int year = parseInt(date, offset, offset += 4);
            if (checkOffset(date, offset, '-')) {
                offset += 1;
            }

            // extract month
            int month = parseInt(date, offset, offset += 2);
            if (checkOffset(date, offset, '-')) {
                offset += 1;
            }

            // extract day
            int day = parseInt(date, offset, offset += 2);
            // default time value
            int hour = 0;
            int minutes = 0;
            int seconds = 0;
            int milliseconds = 0; // always use 0 otherwise returned date will include millis of current time

            // if the value has no time component (and no time zone), we are done
            boolean hasT = checkOffset(date, offset, 'T');
            
            if (!hasT && (date.length() <= offset)) {
                Calendar calendar = new GregorianCalendar(year, month - 1, day);

                pos.setIndex(offset);
                return calendar.getTime();
            }

            if (hasT) {

                // extract hours, minutes, seconds and milliseconds
                hour = parseInt(date, offset += 1, offset += 2);
                if (checkOffset(date, offset, ':')) {
                    offset += 1;
                }

                minutes = parseInt(date, offset, offset += 2);
                if (checkOffset(date, offset, ':')) {
                    offset += 1;
                }
                // second and milliseconds can be optional
                if (date.length() > offset) {
                    char c = date.charAt(offset);
                    if (c != 'Z' && c != '+' && c != '-') {
                        seconds = parseInt(date, offset, offset += 2);
                        if (seconds > 59 && seconds < 63) seconds = 59; // truncate up to 3 leap seconds
                        // milliseconds can be optional in the format
                        if (checkOffset(date, offset, '.')) {
                            offset += 1;
                            int endOffset = indexOfNonDigit(date, offset + 1); // assume at least one digit
                            int parseEndOffset = Math.min(endOffset, offset + 3); // parse up to 3 digits
                            int fraction = parseInt(date, offset, parseEndOffset);
                            // compensate for "missing" digits
                            switch (parseEndOffset - offset) { // number of digits parsed
                            case 2:
                                milliseconds = fraction * 10;
                                break;
                            case 1:
                                milliseconds = fraction * 100;
                                break;
                            default:
                                milliseconds = fraction;
                            }
                            offset = endOffset;
                        }
                    }
                }
            }

            // extract timezone
            if (date.length() <= offset) {
                throw new IllegalArgumentException("No time zone indicator");
            }

            TimeZone timezone = null;
            char timezoneIndicator = date.charAt(offset);

            if (timezoneIndicator == 'Z') {
                timezone = TIMEZONE_UTC;
                offset += 1;
            } else if (timezoneIndicator == '+' || timezoneIndicator == '-') {
                String timezoneOffset = date.substring(offset);

                // When timezone has no minutes, we should append it, valid timezones are, for example: +00:00, +0000 and +00
                timezoneOffset = timezoneOffset.length() >= 5 ? timezoneOffset : timezoneOffset + "00";

                offset += timezoneOffset.length();
                // 18-Jun-2015, tatu: Minor simplification, skip offset of "+0000"/"+00:00"
                if ("+0000".equals(timezoneOffset) || "+00:00".equals(timezoneOffset)) {
                    timezone = TIMEZONE_UTC;
                } else {
                    // 18-Jun-2015, tatu: Looks like offsets only work from GMT, not UTC...
                    //    not sure why, but that's the way it looks. Further, Javadocs for
                    //    `java.util.TimeZone` specifically instruct use of GMT as base for
                    //    custom timezones... odd.
                    String timezoneId = "GMT" + timezoneOffset;
//                    String timezoneId = "UTC" + timezoneOffset;

                    timezone = TimeZone.getTimeZone(timezoneId);

                    String act = timezone.getID();
                    if (!act.equals(timezoneId)) {
                        /* 22-Jan-2015, tatu: Looks like canonical version has colons, but we may be given
                         *    one without. If so, don't sweat.
                         *   Yes, very inefficient. Hopefully not hit often.
                         *   If it becomes a perf problem, add 'loose' comparison instead.
                         */
                        String cleaned = act.replace(":", "");
                        if (!cleaned.equals(timezoneId)) {
                            throw new IndexOutOfBoundsException("Mismatching time zone indicator: "+timezoneId+" given, resolves to "
                                    +timezone.getID());
                        }
                    }
                }
            } else {
                throw new IndexOutOfBoundsException("Invalid time zone indicator '" + timezoneIndicator+"'");
            }

            Calendar calendar = new GregorianCalendar(timezone);
            calendar.setLenient(false);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minutes);
            calendar.set(Calendar.SECOND, seconds);
            calendar.set(Calendar.MILLISECOND, milliseconds);

            pos.setIndex(offset);
            return calendar.getTime();
            // If we get a ParseException it'll already have the right message/offset.
            // Other exception types can convert here.
        } catch (IndexOutOfBoundsException e) {
            fail = e;
        } catch (NumberFormatException e) {
            fail = e;
        } catch (IllegalArgumentException e) {
            fail = e;
        }
        String input = (date == null) ? null : ('"' + date + "'");
        String msg = fail.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = "("+fail.getClass().getName()+")";
        }
        ParseException ex = new ParseException("Failed to parse date [" + input + "]: " + msg, pos.getIndex());
        ex.initCause(fail);
        throw ex;
    }

    /**
     * Check if the expected character exist at the given offset in the value.
     * 
     * @param value the string to check at the specified offset
     * @param offset the offset to look for the expected character
     * @param expected the expected character
     * @return true if the expected character exist at the given offset
     */
    private static boolean checkOffset(String value, int offset, char expected) {
        return (offset < value.length()) && (value.charAt(offset) == expected);
    }

    /**
     * Parse an integer located between 2 given offsets in a string
     * 
     * @param value the string to parse
     * @param beginIndex the start index for the integer in the string
     * @param endIndex the end index for the integer in the string
     * @return the int
     * @throws NumberFormatException if the value is not a number
     */
    private static int parseInt(String value, int beginIndex, int endIndex) throws NumberFormatException {
        if (beginIndex < 0 || endIndex > value.length() || beginIndex > endIndex) {
            throw new NumberFormatException(value);
        }
        // use same logic as in Integer.parseInt() but less generic we're not supporting negative values
        int i = beginIndex;
        int result = 0;
        int digit;
        if (i < endIndex) {
            digit = Character.digit(value.charAt(i++), 10);
            if (digit < 0) {
                throw new NumberFormatException("Invalid number: " + value.substring(beginIndex, endIndex));
            }
            result = -digit;
        }
        while (i < endIndex) {
            digit = Character.digit(value.charAt(i++), 10);
            if (digit < 0) {
                throw new NumberFormatException("Invalid number: " + value.substring(beginIndex, endIndex));
            }
            result *= 10;
            result -= digit;
        }
        return -result;
    }

    /**
     * Zero pad a number to a specified length
     * 
     * @param buffer buffer to use for padding
     * @param value the integer value to pad if necessary.
     * @param length the length of the string we should zero pad
     */
    private static void padInt(StringBuilder buffer, int value, int length) {
        String strValue = Integer.toString(value);
        for (int i = length - strValue.length(); i > 0; i--) {
            buffer.append('0');
        }
        buffer.append(strValue);
    }

    /**
     * Returns the index of the first character in the string that is not a digit, starting at offset.
     */
    private static int indexOfNonDigit(String string, int offset) {
        for (int i = offset; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c < '0' || c > '9') return i;
        }
        return string.length();
    }

}
