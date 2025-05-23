/*
 * Copyright (C) 2008 Google Inc.
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
import com.google.gson.JsonSyntaxException;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for Json primitive values: integers, and floating point numbers.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class PrimitiveTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testPrimitiveIntegerAutoboxedSerialization() {
    assertThat(gson.toJson(1)).isEqualTo("1");
  }

  @Test
  public void testPrimitiveIntegerAutoboxedDeserialization() {
    int expected = 1;
    int actual = gson.fromJson("1", int.class);
    assertThat(actual).isEqualTo(expected);

    actual = gson.fromJson("1", Integer.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testByteSerialization() {
    assertThat(gson.toJson(1, byte.class)).isEqualTo("1");
    assertThat(gson.toJson(1, Byte.class)).isEqualTo("1");
    assertThat(gson.toJson(Byte.MIN_VALUE, Byte.class)).isEqualTo(Byte.toString(Byte.MIN_VALUE));
    assertThat(gson.toJson(Byte.MAX_VALUE, Byte.class)).isEqualTo(Byte.toString(Byte.MAX_VALUE));
    // Should perform narrowing conversion
    assertThat(gson.toJson(128, Byte.class)).isEqualTo("-128");
    assertThat(gson.toJson(1.5, Byte.class)).isEqualTo("1");
  }

  @Test
  public void testByteDeserialization() {
    Byte boxed = gson.fromJson("1", Byte.class);
    assertThat(boxed).isEqualTo(1);
    byte primitive = gson.fromJson("1", byte.class);
    assertThat(primitive).isEqualTo(1);

    byte[] bytes = gson.fromJson("[-128, 0, 127, 255]", byte[].class);
    assertThat(bytes).isEqualTo(new byte[] {-128, 0, 127, -1});
  }

  @Test
  public void testByteDeserializationLossy() {
    JsonSyntaxException e =
        assertThrows(JsonSyntaxException.class, () -> gson.fromJson("-129", byte.class));
    assertThat(e).hasMessageThat().isEqualTo("Lossy conversion from -129 to byte; at path $");

    e = assertThrows(JsonSyntaxException.class, () -> gson.fromJson("256", byte.class));
    assertThat(e).hasMessageThat().isEqualTo("Lossy conversion from 256 to byte; at path $");

    e = assertThrows(JsonSyntaxException.class, () -> gson.fromJson("2147483648", byte.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "java.lang.NumberFormatException: Expected an int but was 2147483648"
                + " at line 1 column 11 path $");
  }

  @Test
  public void testShortSerialization() {
    assertThat(gson.toJson(1, short.class)).isEqualTo("1");
    assertThat(gson.toJson(1, Short.class)).isEqualTo("1");
    assertThat(gson.toJson(Short.MIN_VALUE, Short.class))
        .isEqualTo(Short.toString(Short.MIN_VALUE));
    assertThat(gson.toJson(Short.MAX_VALUE, Short.class))
        .isEqualTo(Short.toString(Short.MAX_VALUE));
    // Should perform widening conversion
    assertThat(gson.toJson((byte) 1, Short.class)).isEqualTo("1");
    // Should perform narrowing conversion
    assertThat(gson.toJson(32768, Short.class)).isEqualTo("-32768");
    assertThat(gson.toJson(1.5, Short.class)).isEqualTo("1");
  }

  @Test
  public void testShortDeserialization() {
    Short boxed = gson.fromJson("1", Short.class);
    assertThat(boxed).isEqualTo(1);
    short primitive = gson.fromJson("1", short.class);
    assertThat(primitive).isEqualTo(1);

    short[] shorts = gson.fromJson("[-32768, 0, 32767, 65535]", short[].class);
    assertThat(shorts).isEqualTo(new short[] {-32768, 0, 32767, -1});
  }

  @Test
  public void testShortDeserializationLossy() {
    JsonSyntaxException e =
        assertThrows(JsonSyntaxException.class, () -> gson.fromJson("-32769", short.class));
    assertThat(e).hasMessageThat().isEqualTo("Lossy conversion from -32769 to short; at path $");

    e = assertThrows(JsonSyntaxException.class, () -> gson.fromJson("65536", short.class));
    assertThat(e).hasMessageThat().isEqualTo("Lossy conversion from 65536 to short; at path $");

    e = assertThrows(JsonSyntaxException.class, () -> gson.fromJson("2147483648", short.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "java.lang.NumberFormatException: Expected an int but was 2147483648"
                + " at line 1 column 11 path $");
  }

  @Test
  public void testIntSerialization() {
    assertThat(gson.toJson(1, int.class)).isEqualTo("1");
    assertThat(gson.toJson(1, Integer.class)).isEqualTo("1");
    assertThat(gson.toJson(Integer.MIN_VALUE, Integer.class))
        .isEqualTo(Integer.toString(Integer.MIN_VALUE));
    assertThat(gson.toJson(Integer.MAX_VALUE, Integer.class))
        .isEqualTo(Integer.toString(Integer.MAX_VALUE));
    // Should perform widening conversion
    assertThat(gson.toJson((byte) 1, Integer.class)).isEqualTo("1");
    // Should perform narrowing conversion
    assertThat(gson.toJson(2147483648L, Integer.class)).isEqualTo("-2147483648");
    assertThat(gson.toJson(1.5, Integer.class)).isEqualTo("1");
  }

  @Test
  public void testLongSerialization() {
    assertThat(gson.toJson(1L, long.class)).isEqualTo("1");
    assertThat(gson.toJson(1L, Long.class)).isEqualTo("1");
    assertThat(gson.toJson(Long.MIN_VALUE, Long.class)).isEqualTo(Long.toString(Long.MIN_VALUE));
    assertThat(gson.toJson(Long.MAX_VALUE, Long.class)).isEqualTo(Long.toString(Long.MAX_VALUE));
    // Should perform widening conversion
    assertThat(gson.toJson((byte) 1, Long.class)).isEqualTo("1");
    // Should perform narrowing conversion
    assertThat(gson.toJson(1.5, Long.class)).isEqualTo("1");
  }

  @Test
  public void testFloatSerialization() {
    assertThat(gson.toJson(1.5f, float.class)).isEqualTo("1.5");
    assertThat(gson.toJson(1.5f, Float.class)).isEqualTo("1.5");
    assertThat(gson.toJson(Float.MIN_VALUE, Float.class))
        .isEqualTo(Float.toString(Float.MIN_VALUE));
    assertThat(gson.toJson(Float.MAX_VALUE, Float.class))
        .isEqualTo(Float.toString(Float.MAX_VALUE));
    // Should perform widening conversion
    assertThat(gson.toJson((byte) 1, Float.class)).isEqualTo("1.0");
    // (This widening conversion is actually lossy)
    assertThat(gson.toJson(Long.MAX_VALUE - 10L, Float.class))
        .isEqualTo(Float.toString((float) (Long.MAX_VALUE - 10L)));
    // Should perform narrowing conversion
    gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    assertThat(gson.toJson(Double.MAX_VALUE, Float.class)).isEqualTo("Infinity");
  }

  @Test
  public void testDoubleSerialization() {
    assertThat(gson.toJson(1.5, double.class)).isEqualTo("1.5");
    assertThat(gson.toJson(1.5, Double.class)).isEqualTo("1.5");
    assertThat(gson.toJson(Double.MIN_VALUE, Double.class))
        .isEqualTo(Double.toString(Double.MIN_VALUE));
    assertThat(gson.toJson(Double.MAX_VALUE, Double.class))
        .isEqualTo(Double.toString(Double.MAX_VALUE));
    // Should perform widening conversion
    assertThat(gson.toJson((byte) 1, Double.class)).isEqualTo("1.0");
    // (This widening conversion is actually lossy)
    assertThat(gson.toJson(Long.MAX_VALUE - 10L, Double.class))
        .isEqualTo(Double.toString((double) (Long.MAX_VALUE - 10L)));
  }

  @Test
  public void testPrimitiveIntegerAutoboxedInASingleElementArraySerialization() {
    int[] target = {-9332};
    assertThat(gson.toJson(target)).isEqualTo("[-9332]");
    assertThat(gson.toJson(target, int[].class)).isEqualTo("[-9332]");
    assertThat(gson.toJson(target, Integer[].class)).isEqualTo("[-9332]");
  }

  @Test
  public void testReallyLongValuesSerialization() {
    long value = 333961828784581L;
    assertThat(gson.toJson(value)).isEqualTo("333961828784581");
  }

  @Test
  public void testReallyLongValuesDeserialization() {
    String json = "333961828784581";
    long value = gson.fromJson(json, Long.class);
    assertThat(value).isEqualTo(333961828784581L);
  }

  @Test
  public void testPrimitiveLongAutoboxedSerialization() {
    assertThat(gson.toJson(1L, long.class)).isEqualTo("1");
    assertThat(gson.toJson(1L, Long.class)).isEqualTo("1");
  }

  @Test
  public void testPrimitiveLongAutoboxedDeserialization() {
    long expected = 1L;
    long actual = gson.fromJson("1", long.class);
    assertThat(actual).isEqualTo(expected);

    actual = gson.fromJson("1", Long.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testPrimitiveLongAutoboxedInASingleElementArraySerialization() {
    long[] target = {-23L};
    assertThat(gson.toJson(target)).isEqualTo("[-23]");
    assertThat(gson.toJson(target, long[].class)).isEqualTo("[-23]");
    assertThat(gson.toJson(target, Long[].class)).isEqualTo("[-23]");
  }

  @Test
  public void testPrimitiveBooleanAutoboxedSerialization() {
    assertThat(gson.toJson(true)).isEqualTo("true");
    assertThat(gson.toJson(false)).isEqualTo("false");
  }

  @Test
  public void testBooleanDeserialization() {
    boolean value = gson.fromJson("false", boolean.class);
    assertThat(value).isEqualTo(false);
    value = gson.fromJson("true", boolean.class);
    assertThat(value).isEqualTo(true);
  }

  @Test
  public void testPrimitiveBooleanAutoboxedInASingleElementArraySerialization() {
    boolean[] target = {false};
    assertThat(gson.toJson(target)).isEqualTo("[false]");
    assertThat(gson.toJson(target, boolean[].class)).isEqualTo("[false]");
    assertThat(gson.toJson(target, Boolean[].class)).isEqualTo("[false]");
  }

  @Test
  public void testNumberSerialization() {
    Number expected = 1L;
    String json = gson.toJson(expected);
    assertThat(json).isEqualTo(expected.toString());

    json = gson.toJson(expected, Number.class);
    assertThat(json).isEqualTo(expected.toString());
  }

  @Test
  public void testNumberDeserialization() {
    String json = "1";
    Number expected = Integer.valueOf(json);
    Number actual = gson.fromJson(json, Number.class);
    assertThat(actual.intValue()).isEqualTo(expected.intValue());

    json = String.valueOf(Long.MAX_VALUE);
    expected = Long.valueOf(json);
    actual = gson.fromJson(json, Number.class);
    assertThat(actual.longValue()).isEqualTo(expected.longValue());

    json = "1.0";
    actual = gson.fromJson(json, Number.class);
    assertThat(actual.longValue()).isEqualTo(1L);
  }

  @Test
  public void testNumberAsStringDeserialization() {
    Number value = gson.fromJson("\"18\"", Number.class);
    assertThat(value.intValue()).isEqualTo(18);
  }

  @Test
  public void testPrimitiveDoubleAutoboxedSerialization() {
    assertThat(gson.toJson(-122.08234335D)).isEqualTo("-122.08234335");
    assertThat(gson.toJson(122.08112002D)).isEqualTo("122.08112002");
  }

  @Test
  public void testPrimitiveDoubleAutoboxedDeserialization() {
    double actual = gson.fromJson("-122.08858585", double.class);
    assertThat(actual).isEqualTo(-122.08858585D);

    actual = gson.fromJson("122.023900008000", Double.class);
    assertThat(actual).isEqualTo(122.023900008D);
  }

  @Test
  public void testPrimitiveDoubleAutoboxedInASingleElementArraySerialization() {
    double[] target = {-122.08D};
    assertThat(gson.toJson(target)).isEqualTo("[-122.08]");
    assertThat(gson.toJson(target, double[].class)).isEqualTo("[-122.08]");
    assertThat(gson.toJson(target, Double[].class)).isEqualTo("[-122.08]");
  }

  @Test
  public void testDoubleAsStringRepresentationDeserialization() {
    String doubleValue = "1.0043E+5";
    Double expected = Double.valueOf(doubleValue);
    Double actual = gson.fromJson(doubleValue, Double.class);
    assertThat(actual).isEqualTo(expected);

    double actual1 = gson.fromJson(doubleValue, double.class);
    assertThat(actual1).isEqualTo(expected);
  }

  @Test
  public void testDoubleNoFractAsStringRepresentationDeserialization() {
    String doubleValue = "1E+5";
    Double expected = Double.valueOf(doubleValue);
    Double actual = gson.fromJson(doubleValue, Double.class);
    assertThat(actual).isEqualTo(expected);

    double actual1 = gson.fromJson(doubleValue, double.class);
    assertThat(actual1).isEqualTo(expected);
  }

  @Test
  public void testDoubleArrayDeserialization() {
    String json =
        "[0.0, 0.004761904761904762, 3.4013606962703525E-4, 7.936508173034305E-4,"
            + "0.0011904761904761906, 0.0]";
    double[] values = gson.fromJson(json, double[].class);

    assertThat(values).hasLength(6);
    assertThat(values[0]).isEqualTo(0.0);
    assertThat(values[1]).isEqualTo(0.004761904761904762);
    assertThat(values[2]).isEqualTo(3.4013606962703525E-4);
    assertThat(values[3]).isEqualTo(7.936508173034305E-4);
    assertThat(values[4]).isEqualTo(0.0011904761904761906);
    assertThat(values[5]).isEqualTo(0.0);
  }

  @Test
  public void testLargeDoubleDeserialization() {
    String doubleValue = "1.234567899E8";
    Double expected = Double.valueOf(doubleValue);
    Double actual = gson.fromJson(doubleValue, Double.class);
    assertThat(actual).isEqualTo(expected);

    double actual1 = gson.fromJson(doubleValue, double.class);
    assertThat(actual1).isEqualTo(expected);
  }

  @Test
  public void testBigDecimalSerialization() {
    BigDecimal target = new BigDecimal("-122.0e-21");
    String json = gson.toJson(target);
    assertThat(new BigDecimal(json)).isEqualTo(target);
  }

  @Test
  public void testBigDecimalDeserialization() {
    BigDecimal target = new BigDecimal("-122.0e-21");
    String json = "-122.0e-21";
    assertThat(gson.fromJson(json, BigDecimal.class)).isEqualTo(target);
  }

  @Test
  public void testBigDecimalInASingleElementArraySerialization() {
    BigDecimal[] target = {new BigDecimal("-122.08e-21")};
    String json = gson.toJson(target);
    String actual = extractElementFromArray(json);
    assertThat(new BigDecimal(actual)).isEqualTo(target[0]);

    json = gson.toJson(target, BigDecimal[].class);
    actual = extractElementFromArray(json);
    assertThat(new BigDecimal(actual)).isEqualTo(target[0]);
  }

  @Test
  public void testSmallValueForBigDecimalSerialization() {
    BigDecimal target = new BigDecimal("1.55");
    String actual = gson.toJson(target);
    assertThat(actual).isEqualTo(target.toString());
  }

  @Test
  public void testSmallValueForBigDecimalDeserialization() {
    BigDecimal expected = new BigDecimal("1.55");
    BigDecimal actual = gson.fromJson("1.55", BigDecimal.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testBigDecimalPreservePrecisionSerialization() {
    String expectedValue = "1.000";
    BigDecimal obj = new BigDecimal(expectedValue);
    String actualValue = gson.toJson(obj);

    assertThat(actualValue).isEqualTo(expectedValue);
  }

  @Test
  public void testBigDecimalPreservePrecisionDeserialization() {
    String json = "1.000";
    BigDecimal expected = new BigDecimal(json);
    BigDecimal actual = gson.fromJson(json, BigDecimal.class);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testBigDecimalAsStringRepresentationDeserialization() {
    String doubleValue = "0.05E+5";
    BigDecimal expected = new BigDecimal(doubleValue);
    BigDecimal actual = gson.fromJson(doubleValue, BigDecimal.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testBigDecimalNoFractAsStringRepresentationDeserialization() {
    String doubleValue = "5E+5";
    BigDecimal expected = new BigDecimal(doubleValue);
    BigDecimal actual = gson.fromJson(doubleValue, BigDecimal.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testBigIntegerSerialization() {
    BigInteger target = new BigInteger("12121211243123245845384534687435634558945453489543985435");
    assertThat(gson.toJson(target)).isEqualTo(target.toString());
  }

  @Test
  public void testBigIntegerDeserialization() {
    String json = "12121211243123245845384534687435634558945453489543985435";
    BigInteger target = new BigInteger(json);
    assertThat(gson.fromJson(json, BigInteger.class)).isEqualTo(target);
  }

  @Test
  public void testBigIntegerInASingleElementArraySerialization() {
    BigInteger[] target = {new BigInteger("1212121243434324323254365345367456456456465464564564")};
    String json = gson.toJson(target);
    String actual = extractElementFromArray(json);
    assertThat(new BigInteger(actual)).isEqualTo(target[0]);

    json = gson.toJson(target, BigInteger[].class);
    actual = extractElementFromArray(json);
    assertThat(new BigInteger(actual)).isEqualTo(target[0]);
  }

  @Test
  public void testSmallValueForBigIntegerSerialization() {
    BigInteger target = new BigInteger("15");
    String actual = gson.toJson(target);
    assertThat(actual).isEqualTo(target.toString());
  }

  @Test
  public void testSmallValueForBigIntegerDeserialization() {
    BigInteger expected = new BigInteger("15");
    BigInteger actual = gson.fromJson("15", BigInteger.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testBadValueForBigIntegerDeserialization() {
    // BigInteger can not be decimal values
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("15.099", BigInteger.class));
  }

  @Test
  public void testLazilyParsedNumberSerialization() {
    LazilyParsedNumber target = new LazilyParsedNumber("1.5");
    String actual = gson.toJson(target);
    assertThat(actual).isEqualTo("1.5");
  }

  @Test
  public void testLazilyParsedNumberDeserialization() {
    LazilyParsedNumber expected = new LazilyParsedNumber("1.5");
    LazilyParsedNumber actual = gson.fromJson("1.5", LazilyParsedNumber.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testMoreSpecificSerialization() {
    Gson gson = new Gson();
    String expected = "This is a string";
    String expectedJson = gson.toJson(expected);

    Serializable serializableString = expected;
    String actualJson = gson.toJson(serializableString, Serializable.class);
    assertThat(actualJson).isNotEqualTo(expectedJson);
  }

  private static String extractElementFromArray(String json) {
    return json.substring(json.indexOf('[') + 1, json.lastIndexOf(']'));
  }

  @Test
  public void testDoubleNaNSerializationNotSupportedByDefault() {
    String expectedMessage =
        "NaN is not a valid double value as per JSON specification. To override this behavior,"
            + " use GsonBuilder.serializeSpecialFloatingPointValues() method.";

    var e =
        assertThrows(IllegalArgumentException.class, () -> gson.toJson(Double.NaN, double.class));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessage);

    e = assertThrows(IllegalArgumentException.class, () -> gson.toJson(Double.NaN, Double.class));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessage);
  }

  @Test
  public void testDoubleNaNSerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    assertThat(gson.toJson(Double.NaN, double.class)).isEqualTo("NaN");
    assertThat(gson.toJson(Double.NaN, Double.class)).isEqualTo("NaN");
  }

  @Test
  public void testDoubleNaNDeserialization() {
    assertThat(gson.fromJson("NaN", double.class)).isNaN();
    assertThat(gson.fromJson("NaN", Double.class)).isNaN();
  }

  @Test
  public void testFloatNaNSerializationNotSupportedByDefault() {
    String expectedMessage =
        "NaN is not a valid double value as per JSON specification. To override this behavior,"
            + " use GsonBuilder.serializeSpecialFloatingPointValues() method.";

    var e = assertThrows(IllegalArgumentException.class, () -> gson.toJson(Float.NaN, float.class));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessage);

    e = assertThrows(IllegalArgumentException.class, () -> gson.toJson(Float.NaN, Float.class));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessage);
  }

  @Test
  public void testFloatNaNSerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    assertThat(gson.toJson(Float.NaN, float.class)).isEqualTo("NaN");
    assertThat(gson.toJson(Float.NaN, Float.class)).isEqualTo("NaN");
  }

  @Test
  public void testFloatNaNDeserialization() {
    assertThat(gson.fromJson("NaN", float.class)).isNaN();
    assertThat(gson.fromJson("NaN", Float.class)).isNaN();
  }

  @Test
  public void testBigDecimalNaNDeserializationNotSupported() {
    // Gson should not accept NaN for deserialization of BigDecimal
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("NaN", BigDecimal.class));
  }

  @Test
  public void testDoubleInfinitySerializationNotSupportedByDefault() {
    String expectedMessage =
        "Infinity is not a valid double value as per JSON specification. To override this"
            + " behavior, use GsonBuilder.serializeSpecialFloatingPointValues() method.";

    var e =
        assertThrows(
            IllegalArgumentException.class,
            () -> gson.toJson(Double.POSITIVE_INFINITY, double.class));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessage);

    e =
        assertThrows(
            IllegalArgumentException.class,
            () -> gson.toJson(Double.POSITIVE_INFINITY, Double.class));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessage);
  }

  @Test
  public void testDoubleInfinitySerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    assertThat(gson.toJson(Double.POSITIVE_INFINITY, double.class)).isEqualTo("Infinity");
    assertThat(gson.toJson(Double.POSITIVE_INFINITY, Double.class)).isEqualTo("Infinity");
  }

  @Test
  public void testDoubleInfinityDeserialization() {
    assertThat(gson.fromJson("Infinity", double.class)).isPositiveInfinity();
    assertThat(gson.fromJson("Infinity", Double.class)).isPositiveInfinity();
  }

  @Test
  public void testFloatInfinitySerializationNotSupportedByDefault() {
    String expectedMessage =
        "Infinity is not a valid double value as per JSON specification. To override this"
            + " behavior, use GsonBuilder.serializeSpecialFloatingPointValues() method.";

    var e =
        assertThrows(
            IllegalArgumentException.class,
            () -> gson.toJson(Float.POSITIVE_INFINITY, float.class));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessage);

    e =
        assertThrows(
            IllegalArgumentException.class,
            () -> gson.toJson(Float.POSITIVE_INFINITY, Float.class));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessage);
  }

  @Test
  public void testFloatInfinitySerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    assertThat(gson.toJson(Float.POSITIVE_INFINITY, float.class)).isEqualTo("Infinity");
    assertThat(gson.toJson(Float.POSITIVE_INFINITY, Float.class)).isEqualTo("Infinity");
  }

  @Test
  public void testFloatInfinityDeserialization() {
    assertThat(gson.fromJson("Infinity", float.class)).isPositiveInfinity();
    assertThat(gson.fromJson("Infinity", Float.class)).isPositiveInfinity();
  }

  @Test
  public void testBigDecimalInfinityDeserializationNotSupported() {
    // Gson should not accept positive infinity for deserialization of BigDecimal
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("Infinity", BigDecimal.class));
  }

  @Test
  public void testNegativeInfinitySerializationNotSupportedByDefault() {
    String expectedMessage =
        "-Infinity is not a valid double value as per JSON specification. To override this"
            + " behavior, use GsonBuilder.serializeSpecialFloatingPointValues() method.";

    var e =
        assertThrows(
            IllegalArgumentException.class,
            () -> gson.toJson(Double.NEGATIVE_INFINITY, double.class));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessage);

    e =
        assertThrows(
            IllegalArgumentException.class,
            () -> gson.toJson(Double.NEGATIVE_INFINITY, Double.class));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessage);
  }

  @Test
  public void testNegativeInfinitySerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    assertThat(gson.toJson(Double.NEGATIVE_INFINITY, double.class)).isEqualTo("-Infinity");
    assertThat(gson.toJson(Double.NEGATIVE_INFINITY, Double.class)).isEqualTo("-Infinity");
  }

  @Test
  public void testNegativeInfinityDeserialization() {
    assertThat(gson.fromJson("-Infinity", double.class)).isNegativeInfinity();
    assertThat(gson.fromJson("-Infinity", Double.class)).isNegativeInfinity();
  }

  @Test
  public void testNegativeInfinityFloatSerializationNotSupportedByDefault() {
    String expectedMessage =
        "-Infinity is not a valid double value as per JSON specification. To override this"
            + " behavior, use GsonBuilder.serializeSpecialFloatingPointValues() method.";

    var e =
        assertThrows(
            IllegalArgumentException.class,
            () -> gson.toJson(Float.NEGATIVE_INFINITY, float.class));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessage);

    e =
        assertThrows(
            IllegalArgumentException.class,
            () -> gson.toJson(Float.NEGATIVE_INFINITY, Float.class));
    assertThat(e).hasMessageThat().isEqualTo(expectedMessage);
  }

  @Test
  public void testNegativeInfinityFloatSerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    assertThat(gson.toJson(Float.NEGATIVE_INFINITY, float.class)).isEqualTo("-Infinity");
    assertThat(gson.toJson(Float.NEGATIVE_INFINITY, Float.class)).isEqualTo("-Infinity");
  }

  @Test
  public void testNegativeInfinityFloatDeserialization() {
    assertThat(gson.fromJson("-Infinity", float.class)).isNegativeInfinity();
    assertThat(gson.fromJson("-Infinity", Float.class)).isNegativeInfinity();
  }

  @Test
  public void testBigDecimalNegativeInfinityDeserializationNotSupported() {
    // Gson should not accept negative infinity for deserialization of BigDecimal
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("-Infinity", BigDecimal.class));
  }

  @Test
  public void testLongAsStringSerialization() {
    gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
    String result = gson.toJson(15L);
    assertThat(result).isEqualTo("\"15\"");

    // Test with an integer and ensure its still a number
    result = gson.toJson(2);
    assertThat(result).isEqualTo("2");
  }

  @Test
  public void testLongAsStringDeserialization() {
    long value = gson.fromJson("\"15\"", long.class);
    assertThat(value).isEqualTo(15);

    gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
    value = gson.fromJson("\"25\"", long.class);
    assertThat(value).isEqualTo(25);
  }

  @Test
  public void testQuotedStringSerializationAndDeserialization() {
    String value = "String Blah Blah Blah...1, 2, 3";
    String serializedForm = gson.toJson(value);
    assertThat(serializedForm).isEqualTo("\"" + value + "\"");

    String actual = gson.fromJson(serializedForm, String.class);
    assertThat(actual).isEqualTo(value);
  }

  @Test
  public void testUnquotedStringDeserializationFails() {
    assertThat(gson.fromJson("UnquotedSingleWord", String.class)).isEqualTo("UnquotedSingleWord");

    String value = "String Blah Blah Blah...1, 2, 3";
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson(value, String.class));
  }

  @Test
  public void testHtmlCharacterSerialization() {
    String target = "<script>var a = 12;</script>";
    String result = gson.toJson(target);
    assertThat(result).isNotEqualTo('"' + target + '"');

    gson = new GsonBuilder().disableHtmlEscaping().create();
    result = gson.toJson(target);
    assertThat(result).isEqualTo('"' + target + '"');
  }

  @Test
  public void testDeserializePrimitiveWrapperAsObjectField() {
    String json = "{i:10}";
    ClassWithIntegerField target = gson.fromJson(json, ClassWithIntegerField.class);
    assertThat(target.i).isEqualTo(10);
  }

  private static class ClassWithIntegerField {
    Integer i;
  }

  @Test
  public void testPrimitiveClassLiteral() {
    assertThat(gson.fromJson("1", int.class)).isEqualTo(1);
    assertThat(gson.fromJson(new StringReader("1"), int.class)).isEqualTo(1);
    assertThat(gson.fromJson(new JsonPrimitive(1), int.class)).isEqualTo(1);
  }

  @Test
  public void testDeserializeJsonObjectAsLongPrimitive() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("{'abc':1}", long.class));
  }

  @Test
  public void testDeserializeJsonArrayAsLongWrapper() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("[1,2,3]", Long.class));
  }

  @Test
  public void testDeserializeJsonArrayAsInt() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("[1, 2, 3, 4]", int.class));
  }

  @Test
  public void testDeserializeJsonObjectAsInteger() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("{}", Integer.class));
  }

  @Test
  public void testDeserializeJsonObjectAsShortPrimitive() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("{'abc':1}", short.class));
  }

  @Test
  public void testDeserializeJsonArrayAsShortWrapper() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("['a','b']", Short.class));
  }

  @Test
  public void testDeserializeJsonArrayAsDoublePrimitive() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("[1,2]", double.class));
  }

  @Test
  public void testDeserializeJsonObjectAsDoubleWrapper() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("{'abc':1}", Double.class));
  }

  @Test
  public void testDeserializeJsonObjectAsFloatPrimitive() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("{'abc':1}", float.class));
  }

  @Test
  public void testDeserializeJsonArrayAsFloatWrapper() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("[1,2,3]", Float.class));
  }

  @Test
  public void testDeserializeJsonObjectAsBytePrimitive() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("{'abc':1}", byte.class));
  }

  @Test
  public void testDeserializeJsonArrayAsByteWrapper() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("[1,2,3,4]", Byte.class));
  }

  @Test
  public void testDeserializeJsonObjectAsBooleanPrimitive() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("{'abc':1}", boolean.class));
  }

  @Test
  public void testDeserializeJsonArrayAsBooleanWrapper() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("[1,2,3,4]", Boolean.class));
  }

  @Test
  public void testDeserializeJsonArrayAsBigDecimal() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("[1,2,3,4]", BigDecimal.class));
  }

  @Test
  public void testDeserializeJsonObjectAsBigDecimal() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("{'a':1}", BigDecimal.class));
  }

  @Test
  public void testDeserializeJsonArrayAsBigInteger() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("[1,2,3,4]", BigInteger.class));
  }

  @Test
  public void testDeserializeJsonObjectAsBigInteger() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("{'c':2}", BigInteger.class));
  }

  @Test
  public void testDeserializeJsonArrayAsNumber() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("[1,2,3,4]", Number.class));
  }

  @Test
  public void testDeserializeJsonObjectAsNumber() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("{'c':2}", Number.class));
  }

  @Test
  public void testDeserializingDecimalPointValueZeroSucceeds() {
    assertThat(gson.fromJson("1.0", Integer.class)).isEqualTo(1);
  }

  @Test
  public void testDeserializingNonZeroDecimalPointValuesAsIntegerFails() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("1.02", Byte.class));
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("1.02", Short.class));
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("1.02", Integer.class));
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("1.02", Long.class));
  }

  @Test
  public void testDeserializingBigDecimalAsIntegerFails() {
    JsonSyntaxException e =
        assertThrows(JsonSyntaxException.class, () -> gson.fromJson("-122.08e-213", Integer.class));
    assertThat(e)
        .hasCauseThat()
        .hasMessageThat()
        .isEqualTo("Expected an int but was -122.08e-213 at line 1 column 13 path $");
  }

  @Test
  public void testDeserializingBigIntegerAsInteger() {
    String number = "12121211243123245845384534687435634558945453489543985435";
    JsonSyntaxException e =
        assertThrows(JsonSyntaxException.class, () -> gson.fromJson(number, Integer.class));
    assertThat(e)
        .hasCauseThat()
        .hasMessageThat()
        .isEqualTo("Expected an int but was " + number + " at line 1 column 57 path $");
  }

  @Test
  public void testDeserializingBigIntegerAsLong() {
    String number = "12121211243123245845384534687435634558945453489543985435";
    JsonSyntaxException e =
        assertThrows(JsonSyntaxException.class, () -> gson.fromJson(number, Long.class));
    assertThat(e)
        .hasCauseThat()
        .hasMessageThat()
        .isEqualTo("Expected a long but was " + number + " at line 1 column 57 path $");
  }

  @Test
  public void testValueVeryCloseToZeroIsZero() {
    assertThat(gson.fromJson("-122.08e-2132", byte.class)).isEqualTo(0);
    assertThat(gson.fromJson("-122.08e-2132", short.class)).isEqualTo(0);
    assertThat(gson.fromJson("-122.08e-2132", int.class)).isEqualTo(0);
    assertThat(gson.fromJson("-122.08e-2132", long.class)).isEqualTo(0);
    assertThat(gson.fromJson("-122.08e-2132", float.class)).isEqualTo(-0.0f);
    assertThat(gson.fromJson("-122.08e-2132", double.class)).isEqualTo(-0.0);
    assertThat(gson.fromJson("122.08e-2132", float.class)).isEqualTo(0.0f);
    assertThat(gson.fromJson("122.08e-2132", double.class)).isEqualTo(0.0);
  }

  @Test
  public void testDeserializingBigDecimalAsBigIntegerFails() {
    assertThrows(JsonSyntaxException.class, () -> gson.fromJson("-122.08e-213", BigInteger.class));
  }

  @Test
  public void testDeserializingBigIntegerAsBigDecimal() {
    BigDecimal actual =
        gson.fromJson("12121211243123245845384534687435634558945453489543985435", BigDecimal.class);
    assertThat(actual.toPlainString())
        .isEqualTo("12121211243123245845384534687435634558945453489543985435");
  }

  @Test
  public void testStringsAsBooleans() {
    String json = "['true', 'false', 'TRUE', 'yes', '1']";
    List<Boolean> deserialized = gson.fromJson(json, new TypeToken<List<Boolean>>() {});
    assertThat(deserialized).isEqualTo(Arrays.asList(true, false, true, false, false));
  }
}
