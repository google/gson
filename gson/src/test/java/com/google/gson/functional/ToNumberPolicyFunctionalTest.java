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
import com.google.gson.JsonPrimitive;
import com.google.gson.Strictness;
import com.google.gson.ToNumberPolicy;
import com.google.gson.ToNumberStrategy;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.junit.Test;

public class ToNumberPolicyFunctionalTest {
  @Test
  public void testDefault() {
    Gson gson = new Gson();
    assertThat(gson.fromJson("null", Object.class)).isNull();
    assertThat(gson.fromJson("10", Object.class)).isEqualTo(10.0);
    assertThat(gson.fromJson("10.0", Object.class)).isEqualTo(10.0);
    assertThat(gson.fromJson("null", Number.class)).isNull();
    assertThat(gson.fromJson("10", Number.class)).isEqualTo(10);
    assertThat(gson.fromJson("10.0", Number.class)).isEqualTo(new LazilyParsedNumber("10.0"));
  }

  @Test
  public void testAsDoubles() {
    Gson gson =
        new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.DOUBLE)
            .setNumberToNumberStrategy(ToNumberPolicy.DOUBLE)
            .create();
    assertThat(gson.fromJson("null", Object.class)).isNull();
    assertThat(gson.fromJson("10", Object.class)).isEqualTo(10.0);
    assertThat(gson.fromJson("null", Number.class)).isNull();
    assertThat(gson.fromJson("10", Number.class)).isEqualTo(10.0);
  }

  @Test
  public void testAsLazilyParsedNumbers() {
    Gson gson =
        new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER)
            .setNumberToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER)
            .create();
    assertThat(gson.fromJson("null", Object.class)).isNull();

    Object number = gson.fromJson("10", Object.class);
    assertThat(number).isEqualTo(10);
    assertThat(number).isInstanceOf(Integer.class);

    number = gson.fromJson(Integer.toString(Integer.MIN_VALUE), Object.class);
    assertThat(number).isEqualTo(Integer.MIN_VALUE);
    assertThat(number).isInstanceOf(Integer.class);

    number = gson.fromJson(Integer.toString(Integer.MAX_VALUE), Object.class);
    assertThat(number).isEqualTo(Integer.MAX_VALUE);
    assertThat(number).isInstanceOf(Integer.class);

    // Integer.MIN_VALUE - 1
    number = gson.fromJson("-2147483649", Object.class);
    assertThat(number).isEqualTo(-2147483649L);
    assertThat(number).isInstanceOf(Long.class);

    // Integer.MAX_VALUE + 1
    number = gson.fromJson("2147483648", Object.class);
    assertThat(number).isEqualTo(2147483648L);
    assertThat(number).isInstanceOf(Long.class);

    number = gson.fromJson(Long.toString(Long.MIN_VALUE), Object.class);
    assertThat(number).isEqualTo(Long.MIN_VALUE);
    assertThat(number).isInstanceOf(Long.class);

    number = gson.fromJson(Long.toString(Long.MAX_VALUE), Object.class);
    assertThat(number).isEqualTo(Long.MAX_VALUE);
    assertThat(number).isInstanceOf(Long.class);

    // Long.MIN_VALUE - 1
    number = gson.fromJson("-9223372036854775809", Object.class);
    assertThat(number).isEqualTo(new LazilyParsedNumber("-9223372036854775809"));
    LazilyParsedNumber lazyNumber = (LazilyParsedNumber) number;
    assertThat(lazyNumber.isGuaranteedValidJsonNumber()).isTrue();

    // Long.MAX_VALUE + 1
    number = gson.fromJson("9223372036854775808", Object.class);
    assertThat(number).isEqualTo(new LazilyParsedNumber("9223372036854775808"));
    lazyNumber = (LazilyParsedNumber) number;
    assertThat(lazyNumber.isGuaranteedValidJsonNumber()).isTrue();

    number = gson.fromJson("10.0", Object.class);
    assertThat(number).isEqualTo(new LazilyParsedNumber("10.0"));
    lazyNumber = (LazilyParsedNumber) number;
    assertThat(lazyNumber.isGuaranteedValidJsonNumber()).isTrue();

    assertThat(gson.fromJson("null", Number.class)).isNull();
    number = gson.fromJson("10", Number.class);
    assertThat(number).isEqualTo(10);
    assertThat(number).isInstanceOf(Integer.class);

    number = gson.fromJson("10.0", Number.class);
    assertThat(number).isEqualTo(new LazilyParsedNumber("10.0"));
    lazyNumber = (LazilyParsedNumber) number;
    assertThat(lazyNumber.isGuaranteedValidJsonNumber()).isTrue();
  }

  @Test
  public void testAsLazilyParsedNumbersString() throws IOException {
    JsonReader jsonReader = new JsonReader(new StringReader("\"10\""));
    Number number = ToNumberPolicy.LAZILY_PARSED_NUMBER.readNumber(jsonReader);
    assertThat(number).isEqualTo(new LazilyParsedNumber("10"));
    LazilyParsedNumber lazyNumber = (LazilyParsedNumber) number;
    // Number read from JSON string value (instead of JSON number) is not trusted
    assertThat(lazyNumber.isGuaranteedValidJsonNumber()).isFalse();
  }

  @Test
  public void testAsLazilyParsedNumbersNonFinite() throws IOException {
    JsonReader jsonReader = new JsonReader(new StringReader("Infinity"));
    jsonReader.setStrictness(Strictness.LENIENT);
    Number number = ToNumberPolicy.LAZILY_PARSED_NUMBER.readNumber(jsonReader);
    assertThat(number).isEqualTo(new LazilyParsedNumber("Infinity"));
    LazilyParsedNumber lazyNumber = (LazilyParsedNumber) number;
    assertThat(lazyNumber.isGuaranteedValidJsonNumber()).isFalse();
  }

  /**
   * Numbers from custom {@link JsonReader} subclass should not be trusted to be valid JSON numbers.
   */
  @Test
  public void testAsLazilyParsedNumbersCustomReader() throws IOException {
    class CustomJsonReader extends JsonReader {
      CustomJsonReader(Reader in) {
        super(in);
      }
    }

    JsonReader jsonReader = new CustomJsonReader(new StringReader("10"));
    Number number = ToNumberPolicy.LAZILY_PARSED_NUMBER.readNumber(jsonReader);
    assertThat(number).isEqualTo(new LazilyParsedNumber("10"));
    LazilyParsedNumber lazyNumber = (LazilyParsedNumber) number;
    assertThat(lazyNumber.isGuaranteedValidJsonNumber()).isFalse();
  }

  /**
   * Checks handling of large JSON numbers.
   *
   * <p><b>Important:</b> This mainly ensures that {@link JsonReader#peek()} only returns {@link
   * JsonToken#NUMBER} when it has actually peeked at the full number. Currently the code assumes
   * that {@code NUMBER} guarantees that the number read using {@link JsonReader#nextString()} is a
   * valid JSON number.
   */
  @Test
  public void testAsLazilyParsedNumbersLargeNumber() throws IOException {
    String numberJson = "1".repeat(10000);
    JsonReader jsonReader = new JsonReader(new StringReader(numberJson));
    // Make lenient because JsonReader currently reads large number as unquoted string
    jsonReader.setStrictness(Strictness.LENIENT);
    // Note: In case future JsonReader refactoring changes the behavior (e.g. supporting arbitrary
    // length JSON numbers), have to verify that `peek() == NUMBER` still guarantees the number is
    // fully valid; otherwise need to change `LAZILY_PARSED_NUMBER` implementation
    Number number = ToNumberPolicy.LAZILY_PARSED_NUMBER.readNumber(jsonReader);
    assertThat(number).isEqualTo(new LazilyParsedNumber(numberJson));
    LazilyParsedNumber lazyNumber = (LazilyParsedNumber) number;
    assertThat(lazyNumber.isGuaranteedValidJsonNumber()).isFalse();

    String invalidNumberJson = numberJson + "a";
    jsonReader = new JsonReader(new StringReader(invalidNumberJson));
    jsonReader.setStrictness(Strictness.LENIENT);
    // Note: Currently `LAZILY_PARSED_NUMBER` itself does not validate the JSON number, it just
    // wraps the unquoted JSON string in a LazilyParsedNumber
    number = ToNumberPolicy.LAZILY_PARSED_NUMBER.readNumber(jsonReader);
    assertThat(number).isEqualTo(new LazilyParsedNumber(invalidNumberJson));
    lazyNumber = (LazilyParsedNumber) number;
    assertThat(lazyNumber.isGuaranteedValidJsonNumber()).isFalse();
  }

  /**
   * Verifies that reading from {@link JsonTreeReader} reused existing number objects, for better
   * performance.
   */
  @Test
  public void testAsLazilyParsedNumbersTreeReader() throws IOException {
    BigDecimal originalNumber = new BigDecimal("1.23");
    JsonReader jsonReader = new JsonTreeReader(new JsonPrimitive(originalNumber));

    Number number = ToNumberPolicy.LAZILY_PARSED_NUMBER.readNumber(jsonReader);
    assertThat(number).isSameInstanceAs(originalNumber);
  }

  @Test
  public void testAsLongsOrDoubles() {
    Gson gson =
        new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create();
    assertThat(gson.fromJson("null", Object.class)).isNull();
    assertThat(gson.fromJson("10", Object.class)).isEqualTo(10L);
    assertThat(gson.fromJson("10.0", Object.class)).isEqualTo(10.0);
    assertThat(gson.fromJson("null", Number.class)).isNull();
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
    assertThat(gson.fromJson("null", Object.class)).isNull();
    assertThat(gson.fromJson("10", Object.class)).isEqualTo(new BigDecimal("10"));
    assertThat(gson.fromJson("10.0", Object.class)).isEqualTo(new BigDecimal("10.0"));
    assertThat(gson.fromJson("null", Number.class)).isNull();
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

    Type type1 = new TypeToken<List<Object>>() {}.getType();
    var e =
        assertThrows(
            UnsupportedOperationException.class, () -> gson.fromJson("[null, 10, 20, 30]", type1));
    assertThat(e).isSameInstanceAs(customException);

    Type type2 = new TypeToken<List<Number>>() {}.getType();
    e =
        assertThrows(
            UnsupportedOperationException.class, () -> gson.fromJson("[null, 10, 20, 30]", type2));
    assertThat(e).isSameInstanceAs(customException);
  }
}
