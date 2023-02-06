/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.stream;

import static com.google.gson.stream.JsonToken.BEGIN_ARRAY;
import static com.google.gson.stream.JsonToken.BEGIN_OBJECT;
import static com.google.gson.stream.JsonToken.BOOLEAN;
import static com.google.gson.stream.JsonToken.END_ARRAY;
import static com.google.gson.stream.JsonToken.END_OBJECT;
import static com.google.gson.stream.JsonToken.NAME;
import static com.google.gson.stream.JsonToken.NULL;
import static com.google.gson.stream.JsonToken.NUMBER;
import static com.google.gson.stream.JsonToken.STRING;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("resource")
public final class JsonReaderTest {
  @Test
  public void testReadArray() throws IOException {
    JsonReader reader = new JsonReader(reader("[true, true]"));
    reader.beginArray();
    assertThat(reader.nextBoolean()).isTrue();
    assertThat(reader.nextBoolean()).isTrue();
    reader.endArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testReadEmptyArray() throws IOException {
    JsonReader reader = new JsonReader(reader("[]"));
    reader.beginArray();
    assertThat(reader.hasNext()).isFalse();
    reader.endArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testReadObject() throws IOException {
    JsonReader reader = new JsonReader(reader(
        "{\"a\": \"android\", \"b\": \"banana\"}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    assertThat(reader.nextString()).isEqualTo("android");
    assertThat(reader.nextName()).isEqualTo("b");
    assertThat(reader.nextString()).isEqualTo("banana");
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testReadEmptyObject() throws IOException {
    JsonReader reader = new JsonReader(reader("{}"));
    reader.beginObject();
    assertThat(reader.hasNext()).isFalse();
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testHasNextEndOfDocument() throws IOException {
    JsonReader reader = new JsonReader(reader("{}"));
    reader.beginObject();
    reader.endObject();
    assertThat(reader.hasNext()).isFalse();
  }

  @Test
  public void testSkipArray() throws IOException {
    JsonReader reader = new JsonReader(reader(
        "{\"a\": [\"one\", \"two\", \"three\"], \"b\": 123}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    reader.skipValue();
    assertThat(reader.nextName()).isEqualTo("b");
    assertThat(reader.nextInt()).isEqualTo(123);
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testSkipArrayAfterPeek() throws Exception {
    JsonReader reader = new JsonReader(reader(
        "{\"a\": [\"one\", \"two\", \"three\"], \"b\": 123}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    assertThat(reader.peek()).isEqualTo(BEGIN_ARRAY);
    reader.skipValue();
    assertThat(reader.nextName()).isEqualTo("b");
    assertThat(reader.nextInt()).isEqualTo(123);
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testSkipTopLevelObject() throws Exception {
    JsonReader reader = new JsonReader(reader(
        "{\"a\": [\"one\", \"two\", \"three\"], \"b\": 123}"));
    reader.skipValue();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testSkipObject() throws IOException {
    JsonReader reader = new JsonReader(reader(
        "{\"a\": { \"c\": [], \"d\": [true, true, {}] }, \"b\": \"banana\"}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    reader.skipValue();
    assertThat(reader.nextName()).isEqualTo("b");
    reader.skipValue();
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testSkipObjectAfterPeek() throws Exception {
    String json = "{" + "  \"one\": { \"num\": 1 }"
        + ", \"two\": { \"num\": 2 }" + ", \"three\": { \"num\": 3 }" + "}";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("one");
    assertThat(reader.peek()).isEqualTo(BEGIN_OBJECT);
    reader.skipValue();
    assertThat(reader.nextName()).isEqualTo("two");
    assertThat(reader.peek()).isEqualTo(BEGIN_OBJECT);
    reader.skipValue();
    assertThat(reader.nextName()).isEqualTo("three");
    reader.skipValue();
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testSkipObjectName() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\": 1}"));
    reader.beginObject();
    reader.skipValue();
    assertThat(reader.peek()).isEqualTo(JsonToken.NUMBER);
    assertThat(reader.getPath()).isEqualTo("$.<skipped>");
    assertThat(reader.nextInt()).isEqualTo(1);
  }

  @Test
  public void testSkipObjectNameSingleQuoted() throws IOException {
    JsonReader reader = new JsonReader(reader("{'a': 1}"));
    reader.setLenient(true);
    reader.beginObject();
    reader.skipValue();
    assertThat(reader.peek()).isEqualTo(JsonToken.NUMBER);
    assertThat(reader.getPath()).isEqualTo("$.<skipped>");
    assertThat(reader.nextInt()).isEqualTo(1);
  }

  @Test
  public void testSkipObjectNameUnquoted() throws IOException {
    JsonReader reader = new JsonReader(reader("{a: 1}"));
    reader.setLenient(true);
    reader.beginObject();
    reader.skipValue();
    assertThat(reader.peek()).isEqualTo(JsonToken.NUMBER);
    assertThat(reader.getPath()).isEqualTo("$.<skipped>");
    assertThat(reader.nextInt()).isEqualTo(1);
  }

  @Test
  public void testSkipInteger() throws IOException {
    JsonReader reader = new JsonReader(reader(
        "{\"a\":123456789,\"b\":-123456789}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    reader.skipValue();
    assertThat(reader.nextName()).isEqualTo("b");
    reader.skipValue();
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testSkipDouble() throws IOException {
    JsonReader reader = new JsonReader(reader(
        "{\"a\":-123.456e-789,\"b\":123456789.0}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    reader.skipValue();
    assertThat(reader.nextName()).isEqualTo("b");
    reader.skipValue();
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testSkipValueAfterEndOfDocument() throws IOException {
    JsonReader reader = new JsonReader(reader("{}"));
    reader.beginObject();
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);

    assertThat(reader.getPath()).isEqualTo("$");
    reader.skipValue();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
    assertThat(reader.getPath()).isEqualTo("$");
  }

  @Test
  public void testSkipValueAtArrayEnd() throws IOException {
    JsonReader reader = new JsonReader(reader("[]"));
    reader.beginArray();
    reader.skipValue();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
    assertThat(reader.getPath()).isEqualTo("$");
  }

  @Test
  public void testSkipValueAtObjectEnd() throws IOException {
    JsonReader reader = new JsonReader(reader("{}"));
    reader.beginObject();
    reader.skipValue();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
    assertThat(reader.getPath()).isEqualTo("$");
  }

  @Test
  public void testHelloWorld() throws IOException {
    String json = "{\n" +
        "   \"hello\": true,\n" +
        "   \"foo\": [\"world\"]\n" +
        "}";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("hello");
    assertThat(reader.nextBoolean()).isTrue();
    assertThat(reader.nextName()).isEqualTo("foo");
    reader.beginArray();
    assertThat(reader.nextString()).isEqualTo("world");
    reader.endArray();
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testInvalidJsonInput() throws IOException {
    String json = "{\n"
        + "   \"h\\ello\": true,\n"
        + "   \"foo\": [\"world\"]\n"
        + "}";

    JsonReader reader = new JsonReader(reader(json));
    reader.beginObject();
    try {
      reader.nextName();
      fail();
    } catch (IOException expected) {
    }
  }

  @SuppressWarnings("unused")
  @Test
  public void testNulls() {
    try {
      new JsonReader(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testEmptyString() {
    try {
      new JsonReader(reader("")).beginArray();
      fail();
    } catch (IOException expected) {
    }
    try {
      new JsonReader(reader("")).beginObject();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testCharacterUnescaping() throws IOException {
    String json = "[\"a\","
        + "\"a\\\"\","
        + "\"\\\"\","
        + "\":\","
        + "\",\","
        + "\"\\b\","
        + "\"\\f\","
        + "\"\\n\","
        + "\"\\r\","
        + "\"\\t\","
        + "\" \","
        + "\"\\\\\","
        + "\"{\","
        + "\"}\","
        + "\"[\","
        + "\"]\","
        + "\"\\u0000\","
        + "\"\\u0019\","
        + "\"\\u20AC\""
        + "]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    assertThat(reader.nextString()).isEqualTo("a");
    assertThat(reader.nextString()).isEqualTo("a\"");
    assertThat(reader.nextString()).isEqualTo("\"");
    assertThat(reader.nextString()).isEqualTo(":");
    assertThat(reader.nextString()).isEqualTo(",");
    assertThat(reader.nextString()).isEqualTo("\b");
    assertThat(reader.nextString()).isEqualTo("\f");
    assertThat(reader.nextString()).isEqualTo("\n");
    assertThat(reader.nextString()).isEqualTo("\r");
    assertThat(reader.nextString()).isEqualTo("\t");
    assertThat(reader.nextString()).isEqualTo(" ");
    assertThat(reader.nextString()).isEqualTo("\\");
    assertThat(reader.nextString()).isEqualTo("{");
    assertThat(reader.nextString()).isEqualTo("}");
    assertThat(reader.nextString()).isEqualTo("[");
    assertThat(reader.nextString()).isEqualTo("]");
    assertThat(reader.nextString()).isEqualTo("\0");
    assertThat(reader.nextString()).isEqualTo("\u0019");
    assertThat(reader.nextString()).isEqualTo("\u20AC");
    reader.endArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testUnescapingInvalidCharacters() throws IOException {
    String json = "[\"\\u000g\"]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (NumberFormatException expected) {
    }
  }

  @Test
  public void testUnescapingTruncatedCharacters() throws IOException {
    String json = "[\"\\u000";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testUnescapingTruncatedSequence() throws IOException {
    String json = "[\"\\";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testIntegersWithFractionalPartSpecified() throws IOException {
    JsonReader reader = new JsonReader(reader("[1.0,1.0,1.0]"));
    reader.beginArray();
    assertThat(reader.nextDouble()).isEqualTo(1.0);
    assertThat(reader.nextInt()).isEqualTo(1);
    assertThat(reader.nextLong()).isEqualTo(1L);
  }

  @Test
  public void testDoubles() throws IOException {
    String json = "[-0.0,"
        + "1.0,"
        + "1.7976931348623157E308,"
        + "4.9E-324,"
        + "0.0,"
        + "0.00,"
        + "-0.5,"
        + "2.2250738585072014E-308,"
        + "3.141592653589793,"
        + "2.718281828459045,"
        + "0,"
        + "0.01,"
        + "0e0,"
        + "1e+0,"
        + "1e-0,"
        + "1e0000," // leading 0 is allowed for exponent
        + "1e00001,"
        + "1e+1]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    assertThat(reader.nextDouble()).isEqualTo(-0.0);
    assertThat(reader.nextDouble()).isEqualTo(1.0);
    assertThat(reader.nextDouble()).isEqualTo(1.7976931348623157E308);
    assertThat(reader.nextDouble()).isEqualTo(4.9E-324);
    assertThat(reader.nextDouble()).isEqualTo(0.0);
    assertThat(reader.nextDouble()).isEqualTo(0.0);
    assertThat(reader.nextDouble()).isEqualTo(-0.5);
    assertThat(reader.nextDouble()).isEqualTo(2.2250738585072014E-308);
    assertThat(reader.nextDouble()).isEqualTo(3.141592653589793);
    assertThat(reader.nextDouble()).isEqualTo(2.718281828459045);
    assertThat(reader.nextDouble()).isEqualTo(0.0);
    assertThat(reader.nextDouble()).isEqualTo(0.01);
    assertThat(reader.nextDouble()).isEqualTo(0.0);
    assertThat(reader.nextDouble()).isEqualTo(1.0);
    assertThat(reader.nextDouble()).isEqualTo(1.0);
    assertThat(reader.nextDouble()).isEqualTo(1.0);
    assertThat(reader.nextDouble()).isEqualTo(10.0);
    assertThat(reader.nextDouble()).isEqualTo(10.0);
    reader.endArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testStrictNonFiniteDoubles() throws IOException {
    String json = "[NaN]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    try {
      reader.nextDouble();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testStrictQuotedNonFiniteDoubles() throws IOException {
    String json = "[\"NaN\"]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    try {
      reader.nextDouble();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testLenientNonFiniteDoubles() throws IOException {
    String json = "[NaN, -Infinity, Infinity]";
    JsonReader reader = new JsonReader(reader(json));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(Double.isNaN(reader.nextDouble())).isTrue();
    assertThat(reader.nextDouble()).isEqualTo(Double.NEGATIVE_INFINITY);
    assertThat(reader.nextDouble()).isEqualTo(Double.POSITIVE_INFINITY);
    reader.endArray();
  }

  @Test
  public void testLenientQuotedNonFiniteDoubles() throws IOException {
    String json = "[\"NaN\", \"-Infinity\", \"Infinity\"]";
    JsonReader reader = new JsonReader(reader(json));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(Double.isNaN(reader.nextDouble())).isTrue();
    assertThat(reader.nextDouble()).isEqualTo(Double.NEGATIVE_INFINITY);
    assertThat(reader.nextDouble()).isEqualTo(Double.POSITIVE_INFINITY);
    reader.endArray();
  }

  @Test
  public void testStrictNonFiniteDoublesWithSkipValue() throws IOException {
    String json = "[NaN]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testLongs() throws IOException {
    String json = "[0,0,0,"
        + "1,1,1,"
        + "-1,-1,-1,"
        + "-9223372036854775808,"
        + "9223372036854775807]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    assertThat(reader.nextLong()).isEqualTo(0L);
    assertThat(reader.nextInt()).isEqualTo(0);
    assertThat(reader.nextDouble()).isEqualTo(0.0);
    assertThat(reader.nextLong()).isEqualTo(1L);
    assertThat(reader.nextInt()).isEqualTo(1);
    assertThat(reader.nextDouble()).isEqualTo(1.0);
    assertThat(reader.nextLong()).isEqualTo(-1L);
    assertThat(reader.nextInt()).isEqualTo(-1);
    assertThat(reader.nextDouble()).isEqualTo(-1.0);
    try {
      reader.nextInt();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertThat(reader.nextLong()).isEqualTo(Long.MIN_VALUE);
    try {
      reader.nextInt();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertThat(reader.nextLong()).isEqualTo(Long.MAX_VALUE);
    reader.endArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  @Ignore
  public void disabled_testNumberWithOctalPrefix() throws IOException {
    String json = "[01]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
    try {
      reader.nextInt();
      fail();
    } catch (MalformedJsonException expected) {
    }
    try {
      reader.nextLong();
      fail();
    } catch (MalformedJsonException expected) {
    }
    try {
      reader.nextDouble();
      fail();
    } catch (MalformedJsonException expected) {
    }
    assertThat(reader.nextString()).isEqualTo("01");
    reader.endArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testBooleans() throws IOException {
    JsonReader reader = new JsonReader(reader("[true,false]"));
    reader.beginArray();
    assertThat(reader.nextBoolean()).isTrue();
    assertThat(reader.nextBoolean()).isFalse();
    reader.endArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testPeekingUnquotedStringsPrefixedWithBooleans() throws IOException {
    JsonReader reader = new JsonReader(reader("[truey]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(STRING);
    try {
      reader.nextBoolean();
      fail();
    } catch (IllegalStateException expected) {
    }
    assertThat(reader.nextString()).isEqualTo("truey");
    reader.endArray();
  }

  @Test
  public void testMalformedNumbers() throws IOException {
    assertNotANumber("-");
    assertNotANumber(".");

    // plus sign is not allowed for integer part
    assertNotANumber("+1");

    // leading 0 is not allowed for integer part
    assertNotANumber("00");
    assertNotANumber("01");

    // exponent lacks digit
    assertNotANumber("e");
    assertNotANumber("0e");
    assertNotANumber(".e");
    assertNotANumber("0.e");
    assertNotANumber("-.0e");

    // no integer
    assertNotANumber("e1");
    assertNotANumber(".e1");
    assertNotANumber("-e1");

    // trailing characters
    assertNotANumber("1x");
    assertNotANumber("1.1x");
    assertNotANumber("1e1x");
    assertNotANumber("1ex");
    assertNotANumber("1.1ex");
    assertNotANumber("1.1e1x");

    // fraction has no digit
    assertNotANumber("0.");
    assertNotANumber("-0.");
    assertNotANumber("0.e1");
    assertNotANumber("-0.e1");

    // no leading digit
    assertNotANumber(".0");
    assertNotANumber("-.0");
    assertNotANumber(".0e1");
    assertNotANumber("-.0e1");
  }

  private void assertNotANumber(String s) throws IOException {
    JsonReader reader = new JsonReader(reader(s));
    reader.setLenient(true);
    assertThat(reader.peek()).isEqualTo(JsonToken.STRING);
    assertThat(reader.nextString()).isEqualTo(s);

    JsonReader strictReader = new JsonReader(reader(s));
    try {
      strictReader.nextDouble();
      fail("Should have failed reading " + s + " as double");
    } catch (MalformedJsonException e) {
    }
  }

  @Test
  public void testPeekingUnquotedStringsPrefixedWithIntegers() throws IOException {
    JsonReader reader = new JsonReader(reader("[12.34e5x]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(STRING);
    try {
      reader.nextInt();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertThat(reader.nextString()).isEqualTo("12.34e5x");
  }

  @Test
  public void testPeekLongMinValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[-9223372036854775808]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(NUMBER);
    assertThat(reader.nextLong()).isEqualTo(-9223372036854775808L);
  }

  @Test
  public void testPeekLongMaxValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[9223372036854775807]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(NUMBER);
    assertThat(reader.nextLong()).isEqualTo(9223372036854775807L);
  }

  @Test
  public void testLongLargerThanMaxLongThatWrapsAround() throws IOException {
    JsonReader reader = new JsonReader(reader("[22233720368547758070]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(NUMBER);
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
  }

  @Test
  public void testLongLargerThanMinLongThatWrapsAround() throws IOException {
    JsonReader reader = new JsonReader(reader("[-22233720368547758070]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(NUMBER);
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
  }

  /**
   * Issue 1053, negative zero.
   * @throws Exception
   */
  @Test
  public void testNegativeZero() throws Exception {
    JsonReader reader = new JsonReader(reader("[-0]"));
    reader.setLenient(false);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(NUMBER);
    assertThat(reader.nextString()).isEqualTo("-0");
  }

  /**
   * This test fails because there's no double for 9223372036854775808, and our
   * long parsing uses Double.parseDouble() for fractional values.
   */
  @Test
  @Ignore
  public void disabled_testPeekLargerThanLongMaxValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[9223372036854775808]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(NUMBER);
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException e) {
    }
  }

  /**
   * This test fails because there's no double for -9223372036854775809, and our
   * long parsing uses Double.parseDouble() for fractional values.
   */
  @Test
  @Ignore
  public void disabled_testPeekLargerThanLongMinValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[-9223372036854775809]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(NUMBER);
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertThat(reader.nextDouble()).isEqualTo(-9223372036854775809d);
  }

  /**
   * This test fails because there's no double for 9223372036854775806, and
   * our long parsing uses Double.parseDouble() for fractional values.
   */
  @Test
  @Ignore
  public void disabled_testHighPrecisionLong() throws IOException {
    String json = "[9223372036854775806.000]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    assertThat(reader.nextLong()).isEqualTo(9223372036854775806L);
    reader.endArray();
  }

  @Test
  public void testPeekMuchLargerThanLongMinValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[-92233720368547758080]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(NUMBER);
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertThat(reader.nextDouble()).isEqualTo(-92233720368547758080d);
  }

  @Test
  public void testQuotedNumberWithEscape() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"12\u00334\"]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(STRING);
    assertThat(reader.nextInt()).isEqualTo(1234);
  }

  @Test
  public void testMixedCaseLiterals() throws IOException {
    JsonReader reader = new JsonReader(reader("[True,TruE,False,FALSE,NULL,nulL]"));
    reader.beginArray();
    assertThat(reader.nextBoolean()).isTrue();
    assertThat(reader.nextBoolean()).isTrue();
    assertThat(reader.nextBoolean()).isFalse();
    assertThat(reader.nextBoolean()).isFalse();
    reader.nextNull();
    reader.nextNull();
    reader.endArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testMissingValue() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    try {
      reader.nextString();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testPrematureEndOfInput() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":true,"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    assertThat(reader.nextBoolean()).isTrue();
    try {
      reader.nextName();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testPrematurelyClosed() throws IOException {
    try {
      JsonReader reader = new JsonReader(reader("{\"a\":[]}"));
      reader.beginObject();
      reader.close();
      reader.nextName();
      fail();
    } catch (IllegalStateException expected) {
    }

    try {
      JsonReader reader = new JsonReader(reader("{\"a\":[]}"));
      reader.close();
      reader.beginObject();
      fail();
    } catch (IllegalStateException expected) {
    }

    try {
      JsonReader reader = new JsonReader(reader("{\"a\":true}"));
      reader.beginObject();
      reader.nextName();
      reader.peek();
      reader.close();
      reader.nextBoolean();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testNextFailuresDoNotAdvance() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":true}"));
    reader.beginObject();
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {
    }
    assertThat(reader.nextName()).isEqualTo("a");
    try {
      reader.nextName();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.beginArray();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.endArray();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.beginObject();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.endObject();
      fail();
    } catch (IllegalStateException expected) {
    }
    assertThat(reader.nextBoolean()).isTrue();
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.nextName();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.beginArray();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.endArray();
      fail();
    } catch (IllegalStateException expected) {
    }
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
    reader.close();
  }

  @Test
  public void testIntegerMismatchFailuresDoNotAdvance() throws IOException {
    JsonReader reader = new JsonReader(reader("[1.5]"));
    reader.beginArray();
    try {
      reader.nextInt();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertThat(reader.nextDouble()).isEqualTo(1.5d);
    reader.endArray();
  }

  @Test
  public void testStringNullIsNotNull() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"null\"]"));
    reader.beginArray();
    try {
      reader.nextNull();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testNullLiteralIsNotAString() throws IOException {
    JsonReader reader = new JsonReader(reader("[null]"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testStrictNameValueSeparator() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\"=true}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    try {
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(reader("{\"a\"=>true}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    try {
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testLenientNameValueSeparator() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\"=true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    assertThat(reader.nextBoolean()).isTrue();

    reader = new JsonReader(reader("{\"a\"=>true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    assertThat(reader.nextBoolean()).isTrue();
  }

  @Test
  public void testStrictNameValueSeparatorWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\"=true}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(reader("{\"a\"=>true}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testCommentsInStringValue() throws Exception {
    JsonReader reader = new JsonReader(reader("[\"// comment\"]"));
    reader.beginArray();
    assertThat(reader.nextString()).isEqualTo("// comment");
    reader.endArray();

    reader = new JsonReader(reader("{\"a\":\"#someComment\"}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    assertThat(reader.nextString()).isEqualTo("#someComment");
    reader.endObject();

    reader = new JsonReader(reader("{\"#//a\":\"#some //Comment\"}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("#//a");
    assertThat(reader.nextString()).isEqualTo("#some //Comment");
    reader.endObject();
  }

  @Test
  public void testStrictComments() throws IOException {
    JsonReader reader = new JsonReader(reader("[// comment \n true]"));
    reader.beginArray();
    try {
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(reader("[# comment \n true]"));
    reader.beginArray();
    try {
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(reader("[/* comment */ true]"));
    reader.beginArray();
    try {
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testLenientComments() throws IOException {
    JsonReader reader = new JsonReader(reader("[// comment \n true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.nextBoolean()).isTrue();

    reader = new JsonReader(reader("[# comment \n true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.nextBoolean()).isTrue();

    reader = new JsonReader(reader("[/* comment */ true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.nextBoolean()).isTrue();
  }

  @Test
  public void testStrictCommentsWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[// comment \n true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(reader("[# comment \n true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(reader("[/* comment */ true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testStrictUnquotedNames() throws IOException {
    JsonReader reader = new JsonReader(reader("{a:true}"));
    reader.beginObject();
    try {
      reader.nextName();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testLenientUnquotedNames() throws IOException {
    JsonReader reader = new JsonReader(reader("{a:true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
  }

  @Test
  public void testStrictUnquotedNamesWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("{a:true}"));
    reader.beginObject();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testStrictSingleQuotedNames() throws IOException {
    JsonReader reader = new JsonReader(reader("{'a':true}"));
    reader.beginObject();
    try {
      reader.nextName();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testLenientSingleQuotedNames() throws IOException {
    JsonReader reader = new JsonReader(reader("{'a':true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
  }

  @Test
  public void testStrictSingleQuotedNamesWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("{'a':true}"));
    reader.beginObject();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testStrictUnquotedStrings() throws IOException {
    JsonReader reader = new JsonReader(reader("[a]"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testStrictUnquotedStringsWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[a]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testLenientUnquotedStrings() throws IOException {
    JsonReader reader = new JsonReader(reader("[a]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.nextString()).isEqualTo("a");
  }

  @Test
  public void testStrictSingleQuotedStrings() throws IOException {
    JsonReader reader = new JsonReader(reader("['a']"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testLenientSingleQuotedStrings() throws IOException {
    JsonReader reader = new JsonReader(reader("['a']"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.nextString()).isEqualTo("a");
  }

  @Test
  public void testStrictSingleQuotedStringsWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("['a']"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testStrictSemicolonDelimitedArray() throws IOException {
    JsonReader reader = new JsonReader(reader("[true;true]"));
    reader.beginArray();
    try {
      reader.nextBoolean();
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testLenientSemicolonDelimitedArray() throws IOException {
    JsonReader reader = new JsonReader(reader("[true;true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.nextBoolean()).isTrue();
    assertThat(reader.nextBoolean()).isTrue();
  }

  @Test
  public void testStrictSemicolonDelimitedArrayWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[true;true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testStrictSemicolonDelimitedNameValuePair() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":true;\"b\":true}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    try {
      reader.nextBoolean();
      reader.nextName();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testLenientSemicolonDelimitedNameValuePair() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":true;\"b\":true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    assertThat(reader.nextBoolean()).isTrue();
    assertThat(reader.nextName()).isEqualTo("b");
  }

  @Test
  public void testStrictSemicolonDelimitedNameValuePairWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":true;\"b\":true}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    try {
      reader.skipValue();
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testStrictUnnecessaryArraySeparators() throws IOException {
    JsonReader reader = new JsonReader(reader("[true,,true]"));
    reader.beginArray();
    assertThat(reader.nextBoolean()).isTrue();
    try {
      reader.nextNull();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(reader("[,true]"));
    reader.beginArray();
    try {
      reader.nextNull();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(reader("[true,]"));
    reader.beginArray();
    assertThat(reader.nextBoolean()).isTrue();
    try {
      reader.nextNull();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(reader("[,]"));
    reader.beginArray();
    try {
      reader.nextNull();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testLenientUnnecessaryArraySeparators() throws IOException {
    JsonReader reader = new JsonReader(reader("[true,,true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.nextBoolean()).isTrue();
    reader.nextNull();
    assertThat(reader.nextBoolean()).isTrue();
    reader.endArray();

    reader = new JsonReader(reader("[,true]"));
    reader.setLenient(true);
    reader.beginArray();
    reader.nextNull();
    assertThat(reader.nextBoolean()).isTrue();
    reader.endArray();

    reader = new JsonReader(reader("[true,]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.nextBoolean()).isTrue();
    reader.nextNull();
    reader.endArray();

    reader = new JsonReader(reader("[,]"));
    reader.setLenient(true);
    reader.beginArray();
    reader.nextNull();
    reader.nextNull();
    reader.endArray();
  }

  @Test
  public void testStrictUnnecessaryArraySeparatorsWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[true,,true]"));
    reader.beginArray();
    assertThat(reader.nextBoolean()).isTrue();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(reader("[,true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(reader("[true,]"));
    reader.beginArray();
    assertThat(reader.nextBoolean()).isTrue();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(reader("[,]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testStrictMultipleTopLevelValues() throws IOException {
    JsonReader reader = new JsonReader(reader("[] []"));
    reader.beginArray();
    reader.endArray();
    try {
      reader.peek();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testLenientMultipleTopLevelValues() throws IOException {
    JsonReader reader = new JsonReader(reader("[] true {}"));
    reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertThat(reader.nextBoolean()).isTrue();
    reader.beginObject();
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testStrictMultipleTopLevelValuesWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[] []"));
    reader.beginArray();
    reader.endArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testTopLevelValueTypes() throws IOException {
    JsonReader reader1 = new JsonReader(reader("true"));
    assertThat(reader1.nextBoolean()).isTrue();
    assertThat(reader1.peek()).isEqualTo(JsonToken.END_DOCUMENT);

    JsonReader reader2 = new JsonReader(reader("false"));
    assertThat(reader2.nextBoolean()).isFalse();
    assertThat(reader2.peek()).isEqualTo(JsonToken.END_DOCUMENT);

    JsonReader reader3 = new JsonReader(reader("null"));
    assertThat(reader3.peek()).isEqualTo(JsonToken.NULL);
    reader3.nextNull();
    assertThat(reader3.peek()).isEqualTo(JsonToken.END_DOCUMENT);

    JsonReader reader4 = new JsonReader(reader("123"));
    assertThat(reader4.nextInt()).isEqualTo(123);
    assertThat(reader4.peek()).isEqualTo(JsonToken.END_DOCUMENT);

    JsonReader reader5 = new JsonReader(reader("123.4"));
    assertThat(reader5.nextDouble()).isEqualTo(123.4);
    assertThat(reader5.peek()).isEqualTo(JsonToken.END_DOCUMENT);

    JsonReader reader6 = new JsonReader(reader("\"a\""));
    assertThat(reader6.nextString()).isEqualTo("a");
    assertThat(reader6.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testTopLevelValueTypeWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("true"));
    reader.skipValue();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testStrictNonExecutePrefix() {
    JsonReader reader = new JsonReader(reader(")]}'\n []"));
    try {
      reader.beginArray();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testStrictNonExecutePrefixWithSkipValue() {
    JsonReader reader = new JsonReader(reader(")]}'\n []"));
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testLenientNonExecutePrefix() throws IOException {
    JsonReader reader = new JsonReader(reader(")]}'\n []"));
    reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testLenientNonExecutePrefixWithLeadingWhitespace() throws IOException {
    JsonReader reader = new JsonReader(reader("\r\n \t)]}'\n []"));
    reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testLenientPartialNonExecutePrefix() {
    JsonReader reader = new JsonReader(reader(")]}' []"));
    reader.setLenient(true);
    try {
      assertThat(reader.nextString()).isEqualTo(")");
      reader.nextString();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testBomIgnoredAsFirstCharacterOfDocument() throws IOException {
    JsonReader reader = new JsonReader(reader("\ufeff[]"));
    reader.beginArray();
    reader.endArray();
  }

  @Test
  public void testBomForbiddenAsOtherCharacterInDocument() throws IOException {
    JsonReader reader = new JsonReader(reader("[\ufeff]"));
    reader.beginArray();
    try {
      reader.endArray();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testFailWithPosition() throws IOException {
    testFailWithPosition("Expected value at line 6 column 5 path $[1]",
        "[\n\n\n\n\n\"a\",}]");
  }

  @Test
  public void testFailWithPositionGreaterThanBufferSize() throws IOException {
    String spaces = repeat(' ', 8192);
    testFailWithPosition("Expected value at line 6 column 5 path $[1]",
        "[\n\n" + spaces + "\n\n\n\"a\",}]");
  }

  @Test
  public void testFailWithPositionOverSlashSlashEndOfLineComment() throws IOException {
    testFailWithPosition("Expected value at line 5 column 6 path $[1]",
        "\n// foo\n\n//bar\r\n[\"a\",}");
  }

  @Test
  public void testFailWithPositionOverHashEndOfLineComment() throws IOException {
    testFailWithPosition("Expected value at line 5 column 6 path $[1]",
        "\n# foo\n\n#bar\r\n[\"a\",}");
  }

  @Test
  public void testFailWithPositionOverCStyleComment() throws IOException {
    testFailWithPosition("Expected value at line 6 column 12 path $[1]",
        "\n\n/* foo\n*\n*\r\nbar */[\"a\",}");
  }

  @Test
  public void testFailWithPositionOverQuotedString() throws IOException {
    testFailWithPosition("Expected value at line 5 column 3 path $[1]",
        "[\"foo\nbar\r\nbaz\n\",\n  }");
  }

  @Test
  public void testFailWithPositionOverUnquotedString() throws IOException {
    testFailWithPosition("Expected value at line 5 column 2 path $[1]", "[\n\nabcd\n\n,}");
  }

  @Test
  public void testFailWithEscapedNewlineCharacter() throws IOException {
    testFailWithPosition("Expected value at line 5 column 3 path $[1]", "[\n\n\"\\\n\n\",}");
  }

  @Test
  public void testFailWithPositionIsOffsetByBom() throws IOException {
    testFailWithPosition("Expected value at line 1 column 6 path $[1]",
        "\ufeff[\"a\",}]");
  }

  private void testFailWithPosition(String message, String json) throws IOException {
    // Validate that it works reading the string normally.
    JsonReader reader1 = new JsonReader(reader(json));
    reader1.setLenient(true);
    reader1.beginArray();
    reader1.nextString();
    try {
      reader1.peek();
      fail();
    } catch (IOException expected) {
      assertThat(expected.getMessage()).isEqualTo(message);
    }

    // Also validate that it works when skipping.
    JsonReader reader2 = new JsonReader(reader(json));
    reader2.setLenient(true);
    reader2.beginArray();
    reader2.skipValue();
    try {
      reader2.peek();
      fail();
    } catch (IOException expected) {
      assertThat(expected.getMessage()).isEqualTo(message);
    }
  }

  @Test
  public void testFailWithPositionDeepPath() throws IOException {
    JsonReader reader = new JsonReader(reader("[1,{\"a\":[2,3,}"));
    reader.beginArray();
    reader.nextInt();
    reader.beginObject();
    reader.nextName();
    reader.beginArray();
    reader.nextInt();
    reader.nextInt();
    try {
      reader.peek();
      fail();
    } catch (IOException expected) {
      assertThat(expected.getMessage()).isEqualTo("Expected value at line 1 column 14 path $[1].a[2]");
    }
  }

  @Test
  public void testStrictVeryLongNumber() throws IOException {
    JsonReader reader = new JsonReader(reader("[0." + repeat('9', 8192) + "]"));
    reader.beginArray();
    try {
      assertThat(reader.nextDouble()).isEqualTo(1d);
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testLenientVeryLongNumber() throws IOException {
    JsonReader reader = new JsonReader(reader("[0." + repeat('9', 8192) + "]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.STRING);
    assertThat(reader.nextDouble()).isEqualTo(1d);
    reader.endArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testVeryLongUnquotedLiteral() throws IOException {
    String literal = "a" + repeat('b', 8192) + "c";
    JsonReader reader = new JsonReader(reader("[" + literal + "]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.nextString()).isEqualTo(literal);
    reader.endArray();
  }

  @Test
  public void testDeeplyNestedArrays() throws IOException {
    // this is nested 40 levels deep; Gson is tuned for nesting is 30 levels deep or fewer
    JsonReader reader = new JsonReader(reader(
        "[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]"));
    for (int i = 0; i < 40; i++) {
      reader.beginArray();
    }
    assertThat(reader.getPath()).isEqualTo("$[0][0][0][0][0][0][0][0][0][0][0][0][0][0][0]"
        + "[0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0]");
    for (int i = 0; i < 40; i++) {
      reader.endArray();
    }
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testDeeplyNestedObjects() throws IOException {
    // Build a JSON document structured like {"a":{"a":{"a":{"a":true}}}}, but 40 levels deep
    String array = "{\"a\":%s}";
    String json = "true";
    for (int i = 0; i < 40; i++) {
      json = String.format(array, json);
    }

    JsonReader reader = new JsonReader(reader(json));
    for (int i = 0; i < 40; i++) {
      reader.beginObject();
      assertThat(reader.nextName()).isEqualTo("a");
    }
    assertThat(reader.getPath()).isEqualTo("$.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a"
        + ".a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a");
    assertThat(reader.nextBoolean()).isTrue();
    for (int i = 0; i < 40; i++) {
      reader.endObject();
    }
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  // http://code.google.com/p/google-gson/issues/detail?id=409
  @Test
  public void testStringEndingInSlash() throws IOException {
    JsonReader reader = new JsonReader(reader("/"));
    reader.setLenient(true);
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testDocumentWithCommentEndingInSlash() throws IOException {
    JsonReader reader = new JsonReader(reader("/* foo *//"));
    reader.setLenient(true);
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testStringWithLeadingSlash() throws IOException {
    JsonReader reader = new JsonReader(reader("/x"));
    reader.setLenient(true);
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testUnterminatedObject() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":\"android\"x"));
    reader.setLenient(true);
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    assertThat(reader.nextString()).isEqualTo("android");
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testVeryLongQuotedString() throws IOException {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[\"" + string + "\"]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    assertThat(reader.nextString()).isEqualTo(string);
    reader.endArray();
  }

  @Test
  public void testVeryLongUnquotedString() throws IOException {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[" + string + "]";
    JsonReader reader = new JsonReader(reader(json));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.nextString()).isEqualTo(string);
    reader.endArray();
  }

  @Test
  public void testVeryLongUnterminatedString() throws IOException {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[" + string;
    JsonReader reader = new JsonReader(reader(json));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.nextString()).isEqualTo(string);
    try {
      reader.peek();
      fail();
    } catch (EOFException expected) {
    }
  }

  @Test
  public void testSkipVeryLongUnquotedString() throws IOException {
    JsonReader reader = new JsonReader(reader("[" + repeat('x', 8192) + "]"));
    reader.setLenient(true);
    reader.beginArray();
    reader.skipValue();
    reader.endArray();
  }

  @Test
  public void testSkipTopLevelUnquotedString() throws IOException {
    JsonReader reader = new JsonReader(reader(repeat('x', 8192)));
    reader.setLenient(true);
    reader.skipValue();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testSkipVeryLongQuotedString() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"" + repeat('x', 8192) + "\"]"));
    reader.beginArray();
    reader.skipValue();
    reader.endArray();
  }

  @Test
  public void testSkipTopLevelQuotedString() throws IOException {
    JsonReader reader = new JsonReader(reader("\"" + repeat('x', 8192) + "\""));
    reader.setLenient(true);
    reader.skipValue();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testStringAsNumberWithTruncatedExponent() throws IOException {
    JsonReader reader = new JsonReader(reader("[123e]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(STRING);
  }

  @Test
  public void testStringAsNumberWithDigitAndNonDigitExponent() throws IOException {
    JsonReader reader = new JsonReader(reader("[123e4b]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(STRING);
  }

  @Test
  public void testStringAsNumberWithNonDigitExponent() throws IOException {
    JsonReader reader = new JsonReader(reader("[123eb]"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(STRING);
  }

  @Test
  public void testEmptyStringName() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"\":true}"));
    reader.setLenient(true);
    assertThat(reader.peek()).isEqualTo(BEGIN_OBJECT);
    reader.beginObject();
    assertThat(reader.peek()).isEqualTo(NAME);
    assertThat(reader.nextName()).isEqualTo("");
    assertThat(reader.peek()).isEqualTo(JsonToken.BOOLEAN);
    assertThat(reader.nextBoolean()).isTrue();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_OBJECT);
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testStrictExtraCommasInMaps() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":\"b\",}"));
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    assertThat(reader.nextString()).isEqualTo("b");
    try {
      reader.peek();
      fail();
    } catch (IOException expected) {
    }
  }

  @Test
  public void testLenientExtraCommasInMaps() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":\"b\",}"));
    reader.setLenient(true);
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("a");
    assertThat(reader.nextString()).isEqualTo("b");
    try {
      reader.peek();
      fail();
    } catch (IOException expected) {
    }
  }

  private String repeat(char c, int count) {
    char[] array = new char[count];
    Arrays.fill(array, c);
    return new String(array);
  }

  @Test
  public void testMalformedDocuments() throws IOException {
    assertDocument("{]", BEGIN_OBJECT, IOException.class);
    assertDocument("{,", BEGIN_OBJECT, IOException.class);
    assertDocument("{{", BEGIN_OBJECT, IOException.class);
    assertDocument("{[", BEGIN_OBJECT, IOException.class);
    assertDocument("{:", BEGIN_OBJECT, IOException.class);
    assertDocument("{\"name\",", BEGIN_OBJECT, NAME, IOException.class);
    assertDocument("{\"name\",", BEGIN_OBJECT, NAME, IOException.class);
    assertDocument("{\"name\":}", BEGIN_OBJECT, NAME, IOException.class);
    assertDocument("{\"name\"::", BEGIN_OBJECT, NAME, IOException.class);
    assertDocument("{\"name\":,", BEGIN_OBJECT, NAME, IOException.class);
    assertDocument("{\"name\"=}", BEGIN_OBJECT, NAME, IOException.class);
    assertDocument("{\"name\"=>}", BEGIN_OBJECT, NAME, IOException.class);
    assertDocument("{\"name\"=>\"string\":", BEGIN_OBJECT, NAME, STRING, IOException.class);
    assertDocument("{\"name\"=>\"string\"=", BEGIN_OBJECT, NAME, STRING, IOException.class);
    assertDocument("{\"name\"=>\"string\"=>", BEGIN_OBJECT, NAME, STRING, IOException.class);
    assertDocument("{\"name\"=>\"string\",", BEGIN_OBJECT, NAME, STRING, IOException.class);
    assertDocument("{\"name\"=>\"string\",\"name\"", BEGIN_OBJECT, NAME, STRING, NAME);
    assertDocument("[}", BEGIN_ARRAY, IOException.class);
    assertDocument("[,]", BEGIN_ARRAY, NULL, NULL, END_ARRAY);
    assertDocument("{", BEGIN_OBJECT, IOException.class);
    assertDocument("{\"name\"", BEGIN_OBJECT, NAME, IOException.class);
    assertDocument("{\"name\",", BEGIN_OBJECT, NAME, IOException.class);
    assertDocument("{'name'", BEGIN_OBJECT, NAME, IOException.class);
    assertDocument("{'name',", BEGIN_OBJECT, NAME, IOException.class);
    assertDocument("{name", BEGIN_OBJECT, NAME, IOException.class);
    assertDocument("[", BEGIN_ARRAY, IOException.class);
    assertDocument("[string", BEGIN_ARRAY, STRING, IOException.class);
    assertDocument("[\"string\"", BEGIN_ARRAY, STRING, IOException.class);
    assertDocument("['string'", BEGIN_ARRAY, STRING, IOException.class);
    assertDocument("[123", BEGIN_ARRAY, NUMBER, IOException.class);
    assertDocument("[123,", BEGIN_ARRAY, NUMBER, IOException.class);
    assertDocument("{\"name\":123", BEGIN_OBJECT, NAME, NUMBER, IOException.class);
    assertDocument("{\"name\":123,", BEGIN_OBJECT, NAME, NUMBER, IOException.class);
    assertDocument("{\"name\":\"string\"", BEGIN_OBJECT, NAME, STRING, IOException.class);
    assertDocument("{\"name\":\"string\",", BEGIN_OBJECT, NAME, STRING, IOException.class);
    assertDocument("{\"name\":'string'", BEGIN_OBJECT, NAME, STRING, IOException.class);
    assertDocument("{\"name\":'string',", BEGIN_OBJECT, NAME, STRING, IOException.class);
    assertDocument("{\"name\":false", BEGIN_OBJECT, NAME, BOOLEAN, IOException.class);
    assertDocument("{\"name\":false,,", BEGIN_OBJECT, NAME, BOOLEAN, IOException.class);
  }

  /**
   * This test behave slightly differently in Gson 2.2 and earlier. It fails
   * during peek rather than during nextString().
   */
  @Test
  public void testUnterminatedStringFailure() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"string"));
    reader.setLenient(true);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.STRING);
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  /**
   * Regression test for an issue with buffer filling and consumeNonExecutePrefix.
   */
  @Test
  public void testReadAcrossBuffers() throws IOException {
    StringBuilder sb = new StringBuilder("#");
    for (int i = 0; i < JsonReader.BUFFER_SIZE - 3; i++) {
      sb.append(' ');
    }
    sb.append("\n)]}'\n3");
    JsonReader reader = new JsonReader(reader(sb.toString()));
    reader.setLenient(true);
    JsonToken token = reader.peek();
    assertThat(token).isEqualTo(JsonToken.NUMBER);
  }

  private void assertDocument(String document, Object... expectations) throws IOException {
    JsonReader reader = new JsonReader(reader(document));
    reader.setLenient(true);
    for (Object expectation : expectations) {
      if (expectation == BEGIN_OBJECT) {
        reader.beginObject();
      } else if (expectation == BEGIN_ARRAY) {
        reader.beginArray();
      } else if (expectation == END_OBJECT) {
        reader.endObject();
      } else if (expectation == END_ARRAY) {
        reader.endArray();
      } else if (expectation == NAME) {
        assertThat(reader.nextName()).isEqualTo("name");
      } else if (expectation == BOOLEAN) {
        assertThat(reader.nextBoolean()).isFalse();
      } else if (expectation == STRING) {
        assertThat(reader.nextString()).isEqualTo("string");
      } else if (expectation == NUMBER) {
        assertThat(reader.nextInt()).isEqualTo(123);
      } else if (expectation == NULL) {
        reader.nextNull();
      } else if (expectation == IOException.class) {
        try {
          reader.peek();
          fail();
        } catch (IOException expected) {
        }
      } else {
        throw new AssertionError();
      }
    }
  }

  /**
   * Returns a reader that returns one character at a time.
   */
  private Reader reader(final String s) {
    /* if (true) */ return new StringReader(s);
    /* return new Reader() {
      int position = 0;
      @Override public int read(char[] buffer, int offset, int count) throws IOException {
        if (position == s.length()) {
          return -1;
        } else if (count > 0) {
          buffer[offset] = s.charAt(position++);
          return 1;
        } else {
          throw new IllegalArgumentException();
        }
      }
      @Override public void close() throws IOException {
      }
    }; */
  }
}
