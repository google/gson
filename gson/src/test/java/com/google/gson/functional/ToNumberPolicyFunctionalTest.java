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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.ToNumberStrategy;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.junit.Test;

public class ToNumberPolicyFunctionalTest {
  @Test
  public void testDefault() {
    Gson gson = new Gson();
    assertThat(gson.fromJson("null", Object.class)).isEqualTo(null);
    assertThat(gson.fromJson("10", Object.class)).isEqualTo(10D);
    assertThat(gson.fromJson("null", Number.class)).isEqualTo(null);
    assertThat(gson.fromJson("10", Number.class)).isEqualTo(new LazilyParsedNumber("10"));
  }

  @Test
  public void testAsDoubles() {
    Gson gson =
        new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.DOUBLE)
            .setNumberToNumberStrategy(ToNumberPolicy.DOUBLE)
            .create();
    assertThat(gson.fromJson("null", Object.class)).isEqualTo(null);
    assertThat(gson.fromJson("10", Object.class)).isEqualTo(10.0);
    assertThat(gson.fromJson("null", Number.class)).isEqualTo(null);
    assertThat(gson.fromJson("10", Number.class)).isEqualTo(10.0);
  }

  @Test
  public void testAsLazilyParsedNumbers() {
    Gson gson =
        new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER)
            .setNumberToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER)
            .create();
    assertThat(gson.fromJson("null", Object.class)).isEqualTo(null);
    assertThat(gson.fromJson("10", Object.class)).isEqualTo(new LazilyParsedNumber("10"));
    assertThat(gson.fromJson("null", Number.class)).isEqualTo(null);
    assertThat(gson.fromJson("10", Number.class)).isEqualTo(new LazilyParsedNumber("10"));
  }

  @Test
  public void testAsLongsOrDoubles() {
    Gson gson =
        new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create();
    assertThat(gson.fromJson("null", Object.class)).isEqualTo(null);
    assertThat(gson.fromJson("10", Object.class)).isEqualTo(10L);
    assertThat(gson.fromJson("10.0", Object.class)).isEqualTo(10.0);
    assertThat(gson.fromJson("null", Number.class)).isEqualTo(null);
    assertThat(gson.fromJson("10", Number.class)).isEqualTo(10L);
    assertThat(gson.fromJson("10.0", Number.class)).isEqualTo(10.0);
  }

  @Test
  public void testAsBigDecimals() {
    Gson gson =
        new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.BIG_DECIMAL)
            .setNumberToNumberStrategy(ToNumberPolicy.BIG_DECIMAL)
            .create();
    assertThat(gson.fromJson("null", Object.class)).isEqualTo(null);
    assertThat(gson.fromJson("10", Object.class)).isEqualTo(new BigDecimal("10"));
    assertThat(gson.fromJson("10.0", Object.class)).isEqualTo(new BigDecimal("10.0"));
    assertThat(gson.fromJson("null", Number.class)).isEqualTo(null);
    assertThat(gson.fromJson("10", Number.class)).isEqualTo(new BigDecimal("10"));
    assertThat(gson.fromJson("10.0", Number.class)).isEqualTo(new BigDecimal("10.0"));
    assertThat(gson.fromJson("3.141592653589793238462643383279", BigDecimal.class))
        .isEqualTo(new BigDecimal("3.141592653589793238462643383279"));
    assertThat(gson.fromJson("1e400", BigDecimal.class)).isEqualTo(new BigDecimal("1e400"));
  }

  @Test
  public void testAsListOfLongsOrDoubles() {
    Gson gson =
        new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create();
    Type objectCollectionType = new TypeToken<Collection<Object>>() {}.getType();
    Collection<Object> objects = gson.fromJson("[null,10,10.0]", objectCollectionType);
    assertThat(objects).containsExactly(null, 10L, 10.0).inOrder();
    Type numberCollectionType = new TypeToken<Collection<Number>>() {}.getType();
    Collection<Object> numbers = gson.fromJson("[null,10,10.0]", numberCollectionType);
    assertThat(numbers).containsExactly(null, 10L, 10.0).inOrder();
  }

  @Test
  public void testCustomStrategiesCannotAffectConcreteDeclaredNumbers() {
    UnsupportedOperationException customException =
        new UnsupportedOperationException("test-exception");
    ToNumberStrategy fail =
        new ToNumberStrategy() {
          @Override
          public Byte readNumber(JsonReader in) {
            throw customException;
          }
        };
    Gson gson =
        new GsonBuilder().setObjectToNumberStrategy(fail).setNumberToNumberStrategy(fail).create();
    List<Object> numbers =
        gson.fromJson("[null, 10, 20, 30]", new TypeToken<List<Byte>>() {}.getType());
    assertThat(numbers).containsExactly(null, (byte) 10, (byte) 20, (byte) 30).inOrder();

    var e =
        assertThrows(
            UnsupportedOperationException.class,
            () -> gson.fromJson("[null, 10, 20, 30]", new TypeToken<List<Object>>() {}.getType()));
    assertThat(e).isSameInstanceAs(customException);

    e =
        assertThrows(
            UnsupportedOperationException.class,
            () -> gson.fromJson("[null, 10, 20, 30]", new TypeToken<List<Number>>() {}.getType()));
    assertThat(e).isSameInstanceAs(customException);
  }
}
