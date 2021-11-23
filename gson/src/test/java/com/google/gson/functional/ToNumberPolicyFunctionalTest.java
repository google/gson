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

package com.google.gson.functional;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.ToNumberStrategy;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import junit.framework.TestCase;

public class ToNumberPolicyFunctionalTest extends TestCase {
  public void testDefault() {
    Gson gson = new Gson();
    assertEquals(null, gson.fromJson("null", Object.class));
    assertEquals(10D, gson.fromJson("10", Object.class));
    assertEquals(null, gson.fromJson("null", Number.class));
    assertEquals(new LazilyParsedNumber("10"), gson.fromJson("10", Number.class));
  }

  public void testAsDoubles() {
    Gson gson = new GsonBuilder()
        .setObjectToNumberStrategy(ToNumberPolicy.DOUBLE)
        .setNumberToNumberStrategy(ToNumberPolicy.DOUBLE)
        .create();
    assertEquals(null, gson.fromJson("null", Object.class));
    assertEquals(10.0, gson.fromJson("10", Object.class));
    assertEquals(null, gson.fromJson("null", Number.class));
    assertEquals(10.0, gson.fromJson("10", Number.class));
  }

  public void testAsLazilyParsedNumbers() {
    Gson gson = new GsonBuilder()
        .setObjectToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER)
        .setNumberToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER)
        .create();
    assertEquals(null, gson.fromJson("null", Object.class));
    assertEquals(new LazilyParsedNumber("10"), gson.fromJson("10", Object.class));
    assertEquals(null, gson.fromJson("null", Number.class));
    assertEquals(new LazilyParsedNumber("10"), gson.fromJson("10", Number.class));
  }

  public void testAsLongsOrDoubles() {
    Gson gson = new GsonBuilder()
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create();
    assertEquals(null, gson.fromJson("null", Object.class));
    assertEquals(10L, gson.fromJson("10", Object.class));
    assertEquals(10.0, gson.fromJson("10.0", Object.class));
    assertEquals(null, gson.fromJson("null", Number.class));
    assertEquals(10L, gson.fromJson("10", Number.class));
    assertEquals(10.0, gson.fromJson("10.0", Number.class));
  }

  public void testAsBigDecimals() {
    Gson gson = new GsonBuilder()
        .setObjectToNumberStrategy(ToNumberPolicy.BIG_DECIMAL)
        .setNumberToNumberStrategy(ToNumberPolicy.BIG_DECIMAL)
        .create();
    assertEquals(null, gson.fromJson("null", Object.class));
    assertEquals(new BigDecimal("10"), gson.fromJson("10", Object.class));
    assertEquals(new BigDecimal("10.0"), gson.fromJson("10.0", Object.class));
    assertEquals(null, gson.fromJson("null", Number.class));
    assertEquals(new BigDecimal("10"), gson.fromJson("10", Number.class));
    assertEquals(new BigDecimal("10.0"), gson.fromJson("10.0", Number.class));
    assertEquals(new BigDecimal("3.141592653589793238462643383279"), gson.fromJson("3.141592653589793238462643383279", BigDecimal.class));
    assertEquals(new BigDecimal("1e400"), gson.fromJson("1e400", BigDecimal.class));
  }

  public void testAsListOfLongsOrDoubles() {
    Gson gson = new GsonBuilder()
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create();
    List<Object> expected = new LinkedList<Object>();
    expected.add(null);
    expected.add(10L);
    expected.add(10.0);
    Type objectCollectionType = new TypeToken<Collection<Object>>() { }.getType();
    Collection<Object> objects = gson.fromJson("[null,10,10.0]", objectCollectionType);
    assertEquals(expected, objects);
    Type numberCollectionType = new TypeToken<Collection<Number>>() { }.getType();
    Collection<Object> numbers = gson.fromJson("[null,10,10.0]", numberCollectionType);
    assertEquals(expected, numbers);
  }

  public void testCustomStrategiesCannotAffectConcreteDeclaredNumbers() {
    ToNumberStrategy fail = new ToNumberStrategy() {
      @Override
      public Byte readNumber(JsonReader in) {
        throw new UnsupportedOperationException();
      }
    };
    Gson gson = new GsonBuilder()
        .setObjectToNumberStrategy(fail)
        .setNumberToNumberStrategy(fail)
        .create();
    List<Object> numbers = gson.fromJson("[null, 10, 20, 30]", new TypeToken<List<Byte>>() {}.getType());
    assertEquals(Arrays.asList(null, (byte) 10, (byte) 20, (byte) 30), numbers);
    try {
      gson.fromJson("[null, 10, 20, 30]", new TypeToken<List<Object>>() {}.getType());
      fail();
    } catch (UnsupportedOperationException ex) {
    }
    try {
      gson.fromJson("[null, 10, 20, 30]", new TypeToken<List<Number>>() {}.getType());
      fail();
    } catch (UnsupportedOperationException ex) {
    }
  }
}
