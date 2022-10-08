/*
 * Copyright (C) 2021 Google Inc.
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

package com.google.gson;

import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * An enumeration that defines two standard number reading strategies and a couple of
 * strategies to overcome some historical Gson limitations while deserializing numbers as
 * {@link Object} and {@link Number}.
 *
 * @see ToNumberStrategy
 * @since 2.8.9
 */
public enum ToNumberPolicy implements ToNumberStrategy {

  /**
   * Using this policy will ensure that numbers will be read as {@link Double} values.
   * This is the default strategy used during deserialization of numbers as {@link Object}.
   */
  DOUBLE {
    @Override public Double readNumber(JsonReader in) throws IOException {
      return in.nextDouble();
    }
  },

  /**
   * Using this policy will ensure that numbers will be read as a lazily parsed number backed
   * by a string. This is the default strategy used during deserialization of numbers as
   * {@link Number}.
   */
  LAZILY_PARSED_NUMBER {
    @Override public Number readNumber(JsonReader in) throws IOException {
      return new LazilyParsedNumber(in.nextString());
    }
  },

  /**
   * Using this policy will ensure that numbers will be read as {@link Long} or {@link Double}
   * values depending on how JSON numbers are represented: {@code Long} if the JSON number can
   * be parsed as a {@code Long} value, or otherwise {@code Double} if it can be parsed as a
   * {@code Double} value. If the parsed double-precision number results in a positive or negative
   * infinity ({@link Double#isInfinite()}) or a NaN ({@link Double#isNaN()}) value and the
   * {@code JsonReader} is not {@link JsonReader#isLenient() lenient}, a {@link MalformedJsonException}
   * is thrown.
   */
  LONG_OR_DOUBLE {
    @Override public Number readNumber(JsonReader in) throws IOException, JsonParseException {
      String value = in.nextString();
      try {
        return Long.parseLong(value);
      } catch (NumberFormatException longE) {
        try {
          Double d = Double.valueOf(value);
          if ((d.isInfinite() || d.isNaN()) && !in.isLenient()) {
            throw new MalformedJsonException("JSON forbids NaN and infinities: " + d + "; at path " + in.getPreviousPath());
          }
          return d;
        } catch (NumberFormatException doubleE) {
          throw new JsonParseException("Cannot parse " + value + "; at path " + in.getPreviousPath(), doubleE);
        }
      }
    }
  },

  /**
   * Using this policy will ensure that numbers will be read as numbers of arbitrary length
   * using {@link BigDecimal}.
   */
  BIG_DECIMAL {
    @Override public BigDecimal readNumber(JsonReader in) throws IOException {
      String value = in.nextString();
      try {
        return new BigDecimal(value);
      } catch (NumberFormatException e) {
        throw new JsonParseException("Cannot parse " + value + "; at path " + in.getPreviousPath(), e);
      }
    }
  }

}
