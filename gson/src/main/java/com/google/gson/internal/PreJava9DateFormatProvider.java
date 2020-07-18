/*
 * Copyright (C) 2017 The Gson authors
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
package com.google.gson.internal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Provides DateFormats for US locale with patterns which were the default ones before Java 9.
 */
public class PreJava9DateFormatProvider {

  /**
   * Returns the same DateFormat as {@code DateFormat.getDateInstance(style, Locale.US)} in Java 8 or below.
   */
  public static DateFormat getUSDateFormat(int style) {
    return new SimpleDateFormat(getDateFormatPattern(style), Locale.US);
  }

  /**
   * Returns the same DateFormat as {@code DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US)}
   * in Java 8 or below.
   */
  public static DateFormat getUSDateTimeFormat(int dateStyle, int timeStyle) {
    String pattern = getDatePartOfDateTimePattern(dateStyle) + " " + getTimePartOfDateTimePattern(timeStyle);
    return new SimpleDateFormat(pattern, Locale.US);
  }

  private static String getDateFormatPattern(int style) {
    switch (style) {
    case DateFormat.SHORT:
      return "M/d/yy";
    case DateFormat.MEDIUM:
      return "MMM d, y";
    case DateFormat.LONG:
      return "MMMM d, y";
    case DateFormat.FULL:
      return "EEEE, MMMM d, y";
    default:
      throw new IllegalArgumentException("Unknown DateFormat style: " + style);
    }
  }

  private static String getDatePartOfDateTimePattern(int dateStyle) {
    switch (dateStyle) {
    case DateFormat.SHORT:
      return "M/d/yy";
    case DateFormat.MEDIUM:
      return "MMM d, yyyy";
    case DateFormat.LONG:
      return "MMMM d, yyyy";
    case DateFormat.FULL:
      return "EEEE, MMMM d, yyyy";
    default:
      throw new IllegalArgumentException("Unknown DateFormat style: " + dateStyle);
    }
  }

  private static String getTimePartOfDateTimePattern(int timeStyle) {
    switch (timeStyle) {
    case DateFormat.SHORT:
      return "h:mm a";
    case DateFormat.MEDIUM:
      return "h:mm:ss a";
    case DateFormat.FULL:
    case DateFormat.LONG:
      return "h:mm:ss a z";
    default:
      throw new IllegalArgumentException("Unknown DateFormat style: " + timeStyle);
    }
  }
}
