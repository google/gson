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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import org.junit.Test;

public class ToNumberPolicyTest {
  @Test
  public void testDouble() throws IOException {
    ToNumberStrategy strategy = ToNumberPolicy.DOUBLE;
    assertThat(strategy.readNumber(fromString("10.1"))).isEqualTo(10.1);
    assertThat(strategy.readNumber(fromString("3.141592653589793238462643383279"))).isEqualTo(3.141592653589793D);
    try {
      strategy.readNumber(fromString("1e400"));
      fail();
    } catch (MalformedJsonException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("JSON forbids NaN and infinities: Infinity at line 1 column 6 path $");
    }
    try {
      strategy.readNumber(fromString("\"not-a-number\""));
      fail();
    } catch (NumberFormatException expected) {
    }
  }

  @Test
  public void testLazilyParsedNumber() throws IOException {
    ToNumberStrategy strategy = ToNumberPolicy.LAZILY_PARSED_NUMBER;
    assertThat(strategy.readNumber(fromString("10.1"))).isEqualTo(new LazilyParsedNumber("10.1"));
    assertThat(strategy.readNumber(fromString("3.141592653589793238462643383279"))).isEqualTo(new LazilyParsedNumber("3.141592653589793238462643383279"));
    assertThat(strategy.readNumber(fromString("1e400"))).isEqualTo(new LazilyParsedNumber("1e400"));
  }

  @Test
  public void testLongOrDouble() throws IOException {
    ToNumberStrategy strategy = ToNumberPolicy.LONG_OR_DOUBLE;
    assertThat(strategy.readNumber(fromString("10"))).isEqualTo(10L);
    assertThat(strategy.readNumber(fromString("10.1"))).isEqualTo(10.1);
    assertThat(strategy.readNumber(fromString("3.141592653589793238462643383279"))).isEqualTo(3.141592653589793D);
    try {
      strategy.readNumber(fromString("1e400"));
      fail();
    } catch (MalformedJsonException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("JSON forbids NaN and infinities: Infinity; at path $");
    }
    try {
      strategy.readNumber(fromString("\"not-a-number\""));
      fail();
    } catch (JsonParseException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Cannot parse not-a-number; at path $");
    }

    assertThat(strategy.readNumber(fromStringLenient("NaN"))).isEqualTo(Double.NaN);
    assertThat(strategy.readNumber(fromStringLenient("Infinity"))).isEqualTo(Double.POSITIVE_INFINITY);
    assertThat(strategy.readNumber(fromStringLenient("-Infinity"))).isEqualTo(Double.NEGATIVE_INFINITY);
    try {
      strategy.readNumber(fromString("NaN"));
      fail();
    } catch (MalformedJsonException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $");
    }
    try {
      strategy.readNumber(fromString("Infinity"));
      fail();
    } catch (MalformedJsonException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $");
    }
    try {
      strategy.readNumber(fromString("-Infinity"));
      fail();
    } catch (MalformedJsonException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $");
    }
  }

  @Test
  public void testBigDecimal() throws IOException {
    ToNumberStrategy strategy = ToNumberPolicy.BIG_DECIMAL;
    assertThat(strategy.readNumber(fromString("10.1"))).isEqualTo(new BigDecimal("10.1"));
    assertThat(strategy.readNumber(fromString("3.141592653589793238462643383279"))).isEqualTo(new BigDecimal("3.141592653589793238462643383279"));
    assertThat(strategy.readNumber(fromString("1e400"))).isEqualTo(new BigDecimal("1e400"));

    try {
      strategy.readNumber(fromString("\"not-a-number\""));
      fail();
    } catch (JsonParseException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Cannot parse not-a-number; at path $");
    }
  }

  @Test
  public void testNullsAreNeverExpected() throws IOException {
    try {
      ToNumberPolicy.DOUBLE.readNumber(fromString("null"));
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      ToNumberPolicy.LAZILY_PARSED_NUMBER.readNumber(fromString("null"));
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      ToNumberPolicy.LONG_OR_DOUBLE.readNumber(fromString("null"));
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      ToNumberPolicy.BIG_DECIMAL.readNumber(fromString("null"));
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  private static JsonReader fromString(String json) {
    return new JsonReader(new StringReader(json));
  }

  private static JsonReader fromStringLenient(String json) {
    JsonReader jsonReader = fromString(json);
    jsonReader.setLenient(true);
    return jsonReader;
  }
}
