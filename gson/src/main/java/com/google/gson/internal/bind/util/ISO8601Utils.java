/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal.bind.util;

import java.text.ParseException;
import java.text.ParsePosition;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;

/**
 * Utilities methods for manipulating dates in iso8601 format. This is much faster and GC friendly
 * than using SimpleDateFormat so highly suitable if you (un)serialize lots of date objects.
 *
 * <p>Supported parse format:
 * [yyyy-MM-dd|yyyyMMdd][T(hh:mm[:ss[.sss]]|hhmm[ss[.sss]])]?[Z|[+-]hh[:]mm]]
 *
 * @see <a href="http://www.w3.org/TR/NOTE-datetime">this specification</a>
 */
// Date parsing code from Jackson databind ISO8601Utils.java
// https://github.com/FasterXML/jackson-databind/blob/2.8/src/main/java/com/fasterxml/jackson/databind/util/ISO8601Utils.java
@SuppressWarnings("MemberName") // legacy class name
public class ISO8601Utils {
  private ISO8601Utils() {}

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
   * @param instant the Instant to format
   * @return the date formatted as 'yyyy-MM-ddThh:mm:ssZ'
   */
  public static String format(Instant instant) {
    return format(instant, false, TIMEZONE_UTC.toZoneId());
  }

  /**
   * Format a date into 'yyyy-MM-ddThh:mm:ss[.sss]Z' (GMT timezone)
   *
   * @param instant the Instant to format
   * @param millis true to include millis precision otherwise false
   * @return the date formatted as 'yyyy-MM-ddThh:mm:ss[.sss]Z'
   */
  public static String format(Instant instant, boolean millis) {
    return format(instant, millis, TIMEZONE_UTC.toZoneId());
  }

  /**
   * Format date into yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm]
   *
   * @param instant the Instant to format
   * @param millis true to include millis precision otherwise false
   * @param zoneId ZoneId to use for the formatting (UTC will produce 'Z')
   * @return the date formatted as yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm]
   */
  public static String format(Instant instant, boolean millis, ZoneId zoneId) {
    ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);

    DateTimeFormatter formatter;
    if (millis) {
      formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    } else {
      formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    }

    return zdt.format(formatter);
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
   * @return the parsed instant
   * @throws ParseException if the date is not in the appropriate format
   */
  public static Instant parse(String date, ParsePosition pos) throws ParseException {
    try {
      int offset = pos.getIndex();
      Instant parsedInstant;

      if (date.contains("T")) {
        // if the value has time component and time zone

        ZonedDateTime zdt =
            ZonedDateTime.parse(date.substring(offset), DateTimeFormatter.ISO_ZONED_DATE_TIME);
        pos.setIndex(date.length());
        parsedInstant = zdt.toInstant();
      } else {
        LocalDate localDate =
            LocalDate.parse(date.substring(offset), DateTimeFormatter.ISO_LOCAL_DATE);
        pos.setIndex(date.length());

        parsedInstant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
      }

      return parsedInstant;
    } catch (DateTimeParseException e) {
      String input = (date == null) ? null : ('"' + date + '"');
      throw new ParseException(
          "Failed to parse date [" + input + "]: " + e.getMessage(), pos.getIndex());
    }
  }
}
