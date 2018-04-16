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

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import junit.framework.TestCase;

public class ToNumberPolicyTest extends TestCase {
  public void testDouble() throws IOException {
    ToNumberStrategy strategy = ToNumberPolicy.DOUBLE;
    assertEquals(10.1, strategy.readNumber(fromString("10.1")));
    assertEquals(3.141592653589793D, strategy.readNumber(fromString("3.141592653589793238462643383279")));
    try {
      strategy.readNumber(fromString("1e400"));
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLazilyParsedNumber() throws IOException {
    ToNumberStrategy strategy = ToNumberPolicy.LAZILY_PARSED_NUMBER;
    assertEquals(new LazilyParsedNumber("10.1"), strategy.readNumber(fromString("10.1")));
    assertEquals(new LazilyParsedNumber("3.141592653589793238462643383279"), strategy.readNumber(fromString("3.141592653589793238462643383279")));
    assertEquals(new LazilyParsedNumber("1e400"), strategy.readNumber(fromString("1e400")));
  }

  public void testLongOrDouble() throws IOException {
    ToNumberStrategy strategy = ToNumberPolicy.LONG_OR_DOUBLE;
    assertEquals(10L, strategy.readNumber(fromString("10")));
    assertEquals(10.1, strategy.readNumber(fromString("10.1")));
    assertEquals(3.141592653589793D, strategy.readNumber(fromString("3.141592653589793238462643383279")));
    try {
      strategy.readNumber(fromString("1e400"));
      fail();
    } catch (MalformedJsonException expected) {
    }
    assertEquals(Double.NaN, strategy.readNumber(fromStringLenient("NaN")));
    assertEquals(Double.POSITIVE_INFINITY, strategy.readNumber(fromStringLenient("Infinity")));
    assertEquals(Double.NEGATIVE_INFINITY, strategy.readNumber(fromStringLenient("-Infinity")));
    try {
      strategy.readNumber(fromString("NaN"));
      fail();
    } catch (MalformedJsonException expected) {
    }
    try {
      strategy.readNumber(fromString("Infinity"));
      fail();
    } catch (MalformedJsonException expected) {
    }
    try {
      strategy.readNumber(fromString("-Infinity"));
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testBigDecimal() throws IOException {
    ToNumberStrategy strategy = ToNumberPolicy.BIG_DECIMAL;
    assertEquals(new BigDecimal("10.1"), strategy.readNumber(fromString("10.1")));
    assertEquals(new BigDecimal("3.141592653589793238462643383279"), strategy.readNumber(fromString("3.141592653589793238462643383279")));
    assertEquals(new BigDecimal("1e400"), strategy.readNumber(fromString("1e400")));
  }

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
