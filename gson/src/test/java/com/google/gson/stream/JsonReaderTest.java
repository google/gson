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

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.stream.JsonReader.StringValueReader;
import com.google.gson.stream.JsonReader.StringValueReaderImpl;

import junit.framework.TestCase;

import static com.google.gson.stream.JsonToken.BEGIN_ARRAY;
import static com.google.gson.stream.JsonToken.BEGIN_OBJECT;
import static com.google.gson.stream.JsonToken.BOOLEAN;
import static com.google.gson.stream.JsonToken.END_ARRAY;
import static com.google.gson.stream.JsonToken.END_OBJECT;
import static com.google.gson.stream.JsonToken.NAME;
import static com.google.gson.stream.JsonToken.NULL;
import static com.google.gson.stream.JsonToken.NUMBER;
import static com.google.gson.stream.JsonToken.STRING;
import static org.junit.Assert.assertArrayEquals;

@SuppressWarnings("resource")
public final class JsonReaderTest extends TestCase {
  public void testReadArray() throws IOException {
    JsonReader reader = new JsonReader(reader("[true, true]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    assertEquals(true, reader.nextBoolean());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testReadEmptyArray() throws IOException {
    JsonReader reader = new JsonReader(reader("[]"));
    reader.beginArray();
    assertFalse(reader.hasNext());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testReadObject() throws IOException {
    JsonReader reader = new JsonReader(reader(
        "{\"a\": \"android\", \"b\": \"banana\"}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals("android", reader.nextString());
    assertEquals("b", reader.nextName());
    assertEquals("banana", reader.nextString());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testReadEmptyObject() throws IOException {
    JsonReader reader = new JsonReader(reader("{}"));
    reader.beginObject();
    assertFalse(reader.hasNext());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipArray() throws IOException {
    JsonReader reader = new JsonReader(reader(
        "{\"a\": [\"one\", \"two\", \"three\"], \"b\": 123}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    reader.skipValue();
    assertEquals("b", reader.nextName());
    assertEquals(123, reader.nextInt());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipArrayAfterPeek() throws Exception {
    JsonReader reader = new JsonReader(reader(
        "{\"a\": [\"one\", \"two\", \"three\"], \"b\": 123}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(BEGIN_ARRAY, reader.peek());
    reader.skipValue();
    assertEquals("b", reader.nextName());
    assertEquals(123, reader.nextInt());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipTopLevelObject() throws Exception {
    JsonReader reader = new JsonReader(reader(
        "{\"a\": [\"one\", \"two\", \"three\"], \"b\": 123}"));
    reader.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipObject() throws IOException {
    JsonReader reader = new JsonReader(reader(
        "{\"a\": { \"c\": [], \"d\": [true, true, {}] }, \"b\": \"banana\"}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    reader.skipValue();
    assertEquals("b", reader.nextName());
    reader.skipValue();
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipObjectAfterPeek() throws Exception {
    String json = "{" + "  \"one\": { \"num\": 1 }"
        + ", \"two\": { \"num\": 2 }" + ", \"three\": { \"num\": 3 }" + "}";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginObject();
    assertEquals("one", reader.nextName());
    assertEquals(BEGIN_OBJECT, reader.peek());
    reader.skipValue();
    assertEquals("two", reader.nextName());
    assertEquals(BEGIN_OBJECT, reader.peek());
    reader.skipValue();
    assertEquals("three", reader.nextName());
    reader.skipValue();
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipInteger() throws IOException {
    JsonReader reader = new JsonReader(reader(
        "{\"a\":123456789,\"b\":-123456789}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    reader.skipValue();
    assertEquals("b", reader.nextName());
    reader.skipValue();
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipDouble() throws IOException {
    JsonReader reader = new JsonReader(reader(
        "{\"a\":-123.456e-789,\"b\":123456789.0}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    reader.skipValue();
    assertEquals("b", reader.nextName());
    reader.skipValue();
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testHelloWorld() throws IOException {
    String json = "{\n" +
        "   \"hello\": true,\n" +
        "   \"foo\": [\"world\"]\n" +
        "}";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginObject();
    assertEquals("hello", reader.nextName());
    assertEquals(true, reader.nextBoolean());
    assertEquals("foo", reader.nextName());
    reader.beginArray();
    assertEquals("world", reader.nextString());
    reader.endArray();
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

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
  public void testNulls() {
    try {
      new JsonReader(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

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
    assertEquals("a", reader.nextString());
    assertEquals("a\"", reader.nextString());
    assertEquals("\"", reader.nextString());
    assertEquals(":", reader.nextString());
    assertEquals(",", reader.nextString());
    assertEquals("\b", reader.nextString());
    assertEquals("\f", reader.nextString());
    assertEquals("\n", reader.nextString());
    assertEquals("\r", reader.nextString());
    assertEquals("\t", reader.nextString());
    assertEquals(" ", reader.nextString());
    assertEquals("\\", reader.nextString());
    assertEquals("{", reader.nextString());
    assertEquals("}", reader.nextString());
    assertEquals("[", reader.nextString());
    assertEquals("]", reader.nextString());
    assertEquals("\0", reader.nextString());
    assertEquals("\u0019", reader.nextString());
    assertEquals("\u20AC", reader.nextString());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

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

  public void testIntegersWithFractionalPartSpecified() throws IOException {
    JsonReader reader = new JsonReader(reader("[1.0,1.0,1.0]"));
    reader.beginArray();
    assertEquals(1.0, reader.nextDouble());
    assertEquals(1, reader.nextInt());
    assertEquals(1L, reader.nextLong());
  }

  public void testDoubles() throws IOException {
    String json = "[-0.0,"
        + "1.0,"
        + "1.7976931348623157E308,"
        + "4.9E-324,"
        + "0.0,"
        + "-0.5,"
        + "2.2250738585072014E-308,"
        + "3.141592653589793,"
        + "2.718281828459045]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    assertEquals(-0.0, reader.nextDouble());
    assertEquals(1.0, reader.nextDouble());
    assertEquals(1.7976931348623157E308, reader.nextDouble());
    assertEquals(4.9E-324, reader.nextDouble());
    assertEquals(0.0, reader.nextDouble());
    assertEquals(-0.5, reader.nextDouble());
    assertEquals(2.2250738585072014E-308, reader.nextDouble());
    assertEquals(3.141592653589793, reader.nextDouble());
    assertEquals(2.718281828459045, reader.nextDouble());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

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

  public void testLenientNonFiniteDoubles() throws IOException {
    String json = "[NaN, -Infinity, Infinity]";
    JsonReader reader = new JsonReader(reader(json));
    reader.setLenient(true);
    reader.beginArray();
    assertTrue(Double.isNaN(reader.nextDouble()));
    assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble());
    assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble());
    reader.endArray();
  }

  public void testLenientQuotedNonFiniteDoubles() throws IOException {
    String json = "[\"NaN\", \"-Infinity\", \"Infinity\"]";
    JsonReader reader = new JsonReader(reader(json));
    reader.setLenient(true);
    reader.beginArray();
    assertTrue(Double.isNaN(reader.nextDouble()));
    assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble());
    assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble());
    reader.endArray();
  }

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

  public void testLongs() throws IOException {
    String json = "[0,0,0,"
        + "1,1,1,"
        + "-1,-1,-1,"
        + "-9223372036854775808,"
        + "9223372036854775807]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    assertEquals(0L, reader.nextLong());
    assertEquals(0, reader.nextInt());
    assertEquals(0.0, reader.nextDouble());
    assertEquals(1L, reader.nextLong());
    assertEquals(1, reader.nextInt());
    assertEquals(1.0, reader.nextDouble());
    assertEquals(-1L, reader.nextLong());
    assertEquals(-1, reader.nextInt());
    assertEquals(-1.0, reader.nextDouble());
    try {
      reader.nextInt();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertEquals(Long.MIN_VALUE, reader.nextLong());
    try {
      reader.nextInt();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertEquals(Long.MAX_VALUE, reader.nextLong());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

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
    assertEquals("01", reader.nextString());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testBooleans() throws IOException {
    JsonReader reader = new JsonReader(reader("[true,false]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    assertEquals(false, reader.nextBoolean());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testPeekingUnquotedStringsPrefixedWithBooleans() throws IOException {
    JsonReader reader = new JsonReader(reader("[truey]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(STRING, reader.peek());
    try {
      reader.nextBoolean();
      fail();
    } catch (IllegalStateException expected) {
    }
    assertEquals("truey", reader.nextString());
    reader.endArray();
  }

  public void testMalformedNumbers() throws IOException {
    assertNotANumber("-");
    assertNotANumber(".");

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
    JsonReader reader = new JsonReader(reader("[" + s + "]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals(s, reader.nextString());
    reader.endArray();
  }

  public void testPeekingUnquotedStringsPrefixedWithIntegers() throws IOException {
    JsonReader reader = new JsonReader(reader("[12.34e5x]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(STRING, reader.peek());
    try {
      reader.nextInt();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertEquals("12.34e5x", reader.nextString());
  }

  public void testPeekLongMinValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[-9223372036854775808]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(NUMBER, reader.peek());
    assertEquals(-9223372036854775808L, reader.nextLong());
  }

  public void testPeekLongMaxValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[9223372036854775807]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(NUMBER, reader.peek());
    assertEquals(9223372036854775807L, reader.nextLong());
  }

  public void testLongLargerThanMaxLongThatWrapsAround() throws IOException {
    JsonReader reader = new JsonReader(reader("[22233720368547758070]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(NUMBER, reader.peek());
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
  }

  public void testLongLargerThanMinLongThatWrapsAround() throws IOException {
    JsonReader reader = new JsonReader(reader("[-22233720368547758070]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(NUMBER, reader.peek());
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
  public void testNegativeZero() throws Exception {
	  	JsonReader reader = new JsonReader(reader("[-0]"));
	    reader.setLenient(false);
	    reader.beginArray();
	    assertEquals(NUMBER, reader.peek());
	    assertEquals("-0", reader.nextString());
  }

  /**
   * This test fails because there's no double for 9223372036854775808, and our
   * long parsing uses Double.parseDouble() for fractional values.
   */
  public void disabled_testPeekLargerThanLongMaxValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[9223372036854775808]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(NUMBER, reader.peek());
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
  public void disabled_testPeekLargerThanLongMinValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[-9223372036854775809]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(NUMBER, reader.peek());
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertEquals(-9223372036854775809d, reader.nextDouble());
  }

  /**
   * This test fails because there's no double for 9223372036854775806, and
   * our long parsing uses Double.parseDouble() for fractional values.
   */
  public void disabled_testHighPrecisionLong() throws IOException {
    String json = "[9223372036854775806.000]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    assertEquals(9223372036854775806L, reader.nextLong());
    reader.endArray();
  }

  public void testPeekMuchLargerThanLongMinValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[-92233720368547758080]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(NUMBER, reader.peek());
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertEquals(-92233720368547758080d, reader.nextDouble());
  }

  public void testQuotedNumberWithEscape() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"12\u00334\"]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(STRING, reader.peek());
    assertEquals(1234, reader.nextInt());
  }

  public void testMixedCaseLiterals() throws IOException {
    JsonReader reader = new JsonReader(reader("[True,TruE,False,FALSE,NULL,nulL]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    assertEquals(true, reader.nextBoolean());
    assertEquals(false, reader.nextBoolean());
    assertEquals(false, reader.nextBoolean());
    reader.nextNull();
    reader.nextNull();
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testMissingValue() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.nextString();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testPrematureEndOfInput() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":true,"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(true, reader.nextBoolean());
    try {
      reader.nextName();
      fail();
    } catch (IOException expected) {
    }
  }

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

  public void testNextFailuresDoNotAdvance() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":true}"));
    reader.beginObject();
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {
    }
    assertEquals("a", reader.nextName());
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
    assertEquals(true, reader.nextBoolean());
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
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    reader.close();
  }

  public void testIntegerMismatchFailuresDoNotAdvance() throws IOException {
    JsonReader reader = new JsonReader(reader("[1.5]"));
    reader.beginArray();
    try {
      reader.nextInt();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertEquals(1.5d, reader.nextDouble());
    reader.endArray();
  }

  public void testStringNullIsNotNull() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"null\"]"));
    reader.beginArray();
    try {
      reader.nextNull();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  public void testNullLiteralIsNotAString() throws IOException {
    JsonReader reader = new JsonReader(reader("[null]"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  public void testStrictNameValueSeparator() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\"=true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(reader("{\"a\"=>true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientNameValueSeparator() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\"=true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(true, reader.nextBoolean());

    reader = new JsonReader(reader("{\"a\"=>true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(true, reader.nextBoolean());
  }

  public void testStrictNameValueSeparatorWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\"=true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(reader("{\"a\"=>true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testCommentsInStringValue() throws Exception {
    JsonReader reader = new JsonReader(reader("[\"// comment\"]"));
    reader.beginArray();
    assertEquals("// comment", reader.nextString());
    reader.endArray();

    reader = new JsonReader(reader("{\"a\":\"#someComment\"}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals("#someComment", reader.nextString());
    reader.endObject();

    reader = new JsonReader(reader("{\"#//a\":\"#some //Comment\"}"));
    reader.beginObject();
    assertEquals("#//a", reader.nextName());
    assertEquals("#some //Comment", reader.nextString());
    reader.endObject();
  }

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

  public void testLenientComments() throws IOException {
    JsonReader reader = new JsonReader(reader("[// comment \n true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());

    reader = new JsonReader(reader("[# comment \n true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());

    reader = new JsonReader(reader("[/* comment */ true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
  }

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

  public void testStrictUnquotedNames() throws IOException {
    JsonReader reader = new JsonReader(reader("{a:true}"));
    reader.beginObject();
    try {
      reader.nextName();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientUnquotedNames() throws IOException {
    JsonReader reader = new JsonReader(reader("{a:true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
  }

  public void testLenientUnquotedNamesReader() throws IOException {
    JsonReader reader = new JsonReader(reader("{a:true}"));
    reader.setLenient(true);
    reader.beginObject();
    Reader nameReader = reader.nextNameReader();
    assertEquals('a', nameReader.read());
    assertEquals(-1, nameReader.read());
  }

  public void testStrictUnquotedNamesWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("{a:true}"));
    reader.beginObject();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictSingleQuotedNames() throws IOException {
    JsonReader reader = new JsonReader(reader("{'a':true}"));
    reader.beginObject();
    try {
      reader.nextName();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictSingleQuotedNamesReader() throws IOException {
    JsonReader reader = new JsonReader(reader("{'a':true}"));
    reader.beginObject();
    try {
      reader.nextNameReader();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientSingleQuotedNames() throws IOException {
    JsonReader reader = new JsonReader(reader("{'a':true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
  }

  public void testLenientSingleQuotedNamesReader() throws IOException {
    JsonReader reader = new JsonReader(reader("{'a':true}"));
    reader.setLenient(true);
    reader.beginObject();
    Reader nameReader = reader.nextNameReader();
    assertEquals('a', nameReader.read());
    assertEquals(-1, nameReader.read());
  }

  public void testStrictSingleQuotedNamesWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("{'a':true}"));
    reader.beginObject();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictUnquotedStrings() throws IOException {
    JsonReader reader = new JsonReader(reader("[a]"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStrictUnquotedStringsReader() throws IOException {
    JsonReader reader = new JsonReader(reader("[a]"));
    reader.beginArray();
    try {
      reader.nextStringReader();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStrictUnquotedStringsWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[a]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientUnquotedStrings() throws IOException {
    JsonReader reader = new JsonReader(reader("[a]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals("a", reader.nextString());
  }

  public void testLenientUnquotedStringsReader() throws IOException {
    JsonReader reader = new JsonReader(reader("[a]"));
    reader.setLenient(true);
    reader.beginArray();
    Reader stringReader = reader.nextStringReader();
    assertEquals('a', stringReader.read());
    assertEquals(-1, stringReader.read());
  }

  public void testStrictSingleQuotedStrings() throws IOException {
    JsonReader reader = new JsonReader(reader("['a']"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientSingleQuotedStrings() throws IOException {
    JsonReader reader = new JsonReader(reader("['a']"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals("a", reader.nextString());
  }

  public void testLenientSingleQuotedStringsReader() throws IOException {
    JsonReader reader = new JsonReader(reader("['a']"));
    reader.setLenient(true);
    reader.beginArray();
    Reader stringReader = reader.nextStringReader();
    assertEquals('a', stringReader.read());
    assertEquals(-1, stringReader.read());
  }

  public void testStrictSingleQuotedStringsWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("['a']"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

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

  public void testLenientSemicolonDelimitedArray() throws IOException {
    JsonReader reader = new JsonReader(reader("[true;true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    assertEquals(true, reader.nextBoolean());
  }

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

  public void testStrictSemicolonDelimitedNameValuePair() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":true;\"b\":true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.nextBoolean();
      reader.nextName();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientSemicolonDelimitedNameValuePair() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":true;\"b\":true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(true, reader.nextBoolean());
    assertEquals("b", reader.nextName());
  }

  public void testStrictSemicolonDelimitedNameValuePairWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":true;\"b\":true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.skipValue();
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictUnnecessaryArraySeparators() throws IOException {
    JsonReader reader = new JsonReader(reader("[true,,true]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
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
    assertEquals(true, reader.nextBoolean());
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

  public void testLenientUnnecessaryArraySeparators() throws IOException {
    JsonReader reader = new JsonReader(reader("[true,,true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    reader.nextNull();
    assertEquals(true, reader.nextBoolean());
    reader.endArray();

    reader = new JsonReader(reader("[,true]"));
    reader.setLenient(true);
    reader.beginArray();
    reader.nextNull();
    assertEquals(true, reader.nextBoolean());
    reader.endArray();

    reader = new JsonReader(reader("[true,]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    reader.nextNull();
    reader.endArray();

    reader = new JsonReader(reader("[,]"));
    reader.setLenient(true);
    reader.beginArray();
    reader.nextNull();
    reader.nextNull();
    reader.endArray();
  }

  public void testStrictUnnecessaryArraySeparatorsWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[true,,true]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
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
    assertEquals(true, reader.nextBoolean());
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

  public void testLenientMultipleTopLevelValues() throws IOException {
    JsonReader reader = new JsonReader(reader("[] true {}"));
    reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertEquals(true, reader.nextBoolean());
    reader.beginObject();
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

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

  public void testTopLevelValueTypes() throws IOException {
    JsonReader reader1 = new JsonReader(reader("true"));
    assertTrue(reader1.nextBoolean());
    assertEquals(JsonToken.END_DOCUMENT, reader1.peek());

    JsonReader reader2 = new JsonReader(reader("false"));
    assertFalse(reader2.nextBoolean());
    assertEquals(JsonToken.END_DOCUMENT, reader2.peek());

    JsonReader reader3 = new JsonReader(reader("null"));
    assertEquals(JsonToken.NULL, reader3.peek());
    reader3.nextNull();
    assertEquals(JsonToken.END_DOCUMENT, reader3.peek());

    JsonReader reader4 = new JsonReader(reader("123"));
    assertEquals(123, reader4.nextInt());
    assertEquals(JsonToken.END_DOCUMENT, reader4.peek());

    JsonReader reader5 = new JsonReader(reader("123.4"));
    assertEquals(123.4, reader5.nextDouble());
    assertEquals(JsonToken.END_DOCUMENT, reader5.peek());

    JsonReader reader6 = new JsonReader(reader("\"a\""));
    assertEquals("a", reader6.nextString());
    assertEquals(JsonToken.END_DOCUMENT, reader6.peek());

    JsonReader reader7 = new JsonReader(reader("\"a\""));
    Reader stringReader = reader7.nextStringReader();
    assertEquals('a', stringReader.read());
    assertEquals(-1, stringReader.read());
    stringReader.close();
    assertEquals(JsonToken.END_DOCUMENT, reader7.peek());
  }

  public void testTopLevelValueTypeWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("true"));
    reader.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testStrictNonExecutePrefix() {
    JsonReader reader = new JsonReader(reader(")]}'\n []"));
    try {
      reader.beginArray();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictNonExecutePrefixWithSkipValue() {
    JsonReader reader = new JsonReader(reader(")]}'\n []"));
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientNonExecutePrefix() throws IOException {
    JsonReader reader = new JsonReader(reader(")]}'\n []"));
    reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testLenientNonExecutePrefixWithLeadingWhitespace() throws IOException {
    JsonReader reader = new JsonReader(reader("\r\n \t)]}'\n []"));
    reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testLenientPartialNonExecutePrefix() throws IOException {
    JsonReader reader = new JsonReader(reader(")]}' []"));
    reader.setLenient(true);
    assertEquals(")", reader.nextString());
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientPartialNonExecutePrefixReader() throws IOException {
    JsonReader reader = new JsonReader(reader(")]}' []"));
    reader.setLenient(true);
    Reader stringReader = reader.nextStringReader();
    assertEquals(')', stringReader.read());
    assertEquals(-1, stringReader.read());
    stringReader.close();
    try {
      reader.nextStringReader();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testBomIgnoredAsFirstCharacterOfDocument() throws IOException {
    JsonReader reader = new JsonReader(reader("\ufeff[]"));
    reader.beginArray();
    reader.endArray();
  }

  public void testBomForbiddenAsOtherCharacterInDocument() throws IOException {
    JsonReader reader = new JsonReader(reader("[\ufeff]"));
    reader.beginArray();
    try {
      reader.endArray();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testFailWithPosition() throws IOException {
    String msg = "Expected value at line 6 column 5 path $[1]";
    String json = "[\n\n\n\n\n\"a\",}]";
    testFailWithPosition(msg, json);
    testFailWithPositionReader(msg, json);
  }

  public void testFailWithPositionGreaterThanBufferSize() throws IOException {
    String msg = "Expected value at line 6 column 5 path $[1]";
    String spaces = repeat(' ', 8192);
    String json = "[\n\n" + spaces + "\n\n\n\"a\",}]";

    testFailWithPosition(msg, json);
    testFailWithPositionReader(msg, json);
  }

  public void testFailWithPositionOverSlashSlashEndOfLineComment() throws IOException {
    String msg = "Expected value at line 5 column 6 path $[1]";
    String json = "\n// foo\n\n//bar\r\n[\"a\",}";

    testFailWithPosition(msg, json);
    testFailWithPositionReader(msg, json);
  }

  public void testFailWithPositionOverHashEndOfLineComment() throws IOException {
    String msg = "Expected value at line 5 column 6 path $[1]";
    String json = "\n# foo\n\n#bar\r\n[\"a\",}";

    testFailWithPosition(msg, json);
    testFailWithPositionReader(msg, json);
  }

  public void testFailWithPositionOverCStyleComment() throws IOException {
    String msg = "Expected value at line 6 column 12 path $[1]";
    String json = "\n\n/* foo\n*\n*\r\nbar */[\"a\",}";

    testFailWithPosition(msg, json);
    testFailWithPositionReader(msg, json);
  }

  public void testFailWithPositionOverQuotedString() throws IOException {
    String msg = "Expected value at line 5 column 3 path $[1]";
    String json = "[\"foo\nbar\r\nbaz\n\",\n  }";
    testFailWithPosition(msg, json);
    testFailWithPositionReader(msg, json);
  }

  public void testFailWithPositionOverUnquotedString() throws IOException {
    String msg = "Expected value at line 5 column 2 path $[1]";
    String json = "[\n\nabcd\n\n,}";

    testFailWithPosition(msg, json);
    testFailWithPositionReader(msg, json);
  }

  public void testFailWithEscapedNewlineCharacter() throws IOException {
    String msg = "Expected value at line 5 column 3 path $[1]";
    String json = "[\n\n\"\\\n\n\",}";

    testFailWithPosition(msg, json);
    testFailWithPositionReader(msg, json);
  }

  public void testFailWithPositionIsOffsetByBom() throws IOException {
    String msg = "Expected value at line 1 column 6 path $[1]";
    String json = "\ufeff[\"a\",}]";

    testFailWithPosition(msg, json);
    testFailWithPositionReader(msg, json);
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
      assertEquals(message, expected.getMessage());
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
      assertEquals(message, expected.getMessage());
    }
  }

  private void testFailWithPositionReader(String message, String json) throws IOException {
    JsonReader reader1 = new JsonReader(reader(json));
    reader1.setLenient(true);
    reader1.beginArray();
    // Skip all chars of string value
    Reader stringReader = reader1.nextStringReader();
    while (stringReader.read() != -1) { }
    stringReader.close();
    try {
      reader1.peek();
      fail();
    } catch (IOException expected) {
      assertEquals(message, expected.getMessage());
    }
  }

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
      assertEquals("Expected value at line 1 column 14 path $[1].a[2]", expected.getMessage());
    }
  }

  public void testStrictVeryLongNumber() throws IOException {
    JsonReader reader = new JsonReader(reader("[0." + repeat('9', 8192) + "]"));
    reader.beginArray();
    try {
      assertEquals(1d, reader.nextDouble());
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientVeryLongNumber() throws IOException {
    JsonReader reader = new JsonReader(reader("[0." + repeat('9', 8192) + "]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals(1d, reader.nextDouble());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testVeryLongUnquotedLiteral() throws IOException {
    String literal = "a" + repeat('b', 8192) + "c";
    JsonReader reader = new JsonReader(reader("[" + literal + "]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(literal, reader.nextString());
    reader.endArray();
  }

  public void testDeeplyNestedArrays() throws IOException {
    // this is nested 40 levels deep; Gson is tuned for nesting is 30 levels deep or fewer
    JsonReader reader = new JsonReader(reader(
        "[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]"));
    for (int i = 0; i < 40; i++) {
      reader.beginArray();
    }
    assertEquals("$[0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0][0]"
        + "[0][0][0][0][0][0][0][0][0][0][0][0][0][0]", reader.getPath());
    for (int i = 0; i < 40; i++) {
      reader.endArray();
    }
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

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
      assertEquals("a", reader.nextName());
    }
    assertEquals("$.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a"
        + ".a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a", reader.getPath());
    assertEquals(true, reader.nextBoolean());
    for (int i = 0; i < 40; i++) {
      reader.endObject();
    }
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  // http://code.google.com/p/google-gson/issues/detail?id=409
  public void testStringEndingInSlash() throws IOException {
    JsonReader reader = new JsonReader(reader("/"));
    reader.setLenient(true);
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testDocumentWithCommentEndingInSlash() throws IOException {
    JsonReader reader = new JsonReader(reader("/* foo *//"));
    reader.setLenient(true);
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStringWithLeadingSlash() throws IOException {
    JsonReader reader = new JsonReader(reader("/x"));
    reader.setLenient(true);
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testUnterminatedObject() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":\"android\"x"));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals("android", reader.nextString());
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testVeryLongQuotedString() throws IOException {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[\"" + string + "\"]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    assertEquals(string, reader.nextString());
    reader.endArray();
  }

  public void testVeryLongQuotedStringReader() throws IOException {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[\"" + string + "\"]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();

    Reader stringReader = reader.nextStringReader();
    char[] readChars = new char[stringChars.length];
    while (stringReader.read(readChars) != -1) { }
    stringReader.close();
    assertArrayEquals(stringChars, readChars);

    reader.endArray();
  }

  public void testVeryLongUnquotedString() throws IOException {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[" + string + "]";
    JsonReader reader = new JsonReader(reader(json));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(string, reader.nextString());
    reader.endArray();
  }

  public void testVeryLongUnquotedStringReader() throws IOException {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[" + string + "]";
    JsonReader reader = new JsonReader(reader(json));
    reader.setLenient(true);
    reader.beginArray();

    Reader stringReader = reader.nextStringReader();
    char[] readChars = new char[stringChars.length];
    while (stringReader.read(readChars) != -1) { }
    stringReader.close();
    assertArrayEquals(stringChars, readChars);

    reader.endArray();
  }

  public void testVeryLongUnterminatedString() throws IOException {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[" + string;
    JsonReader reader = new JsonReader(reader(json));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(string, reader.nextString());
    try {
      reader.peek();
      fail();
    } catch (EOFException expected) {
    }
  }

  public void testVeryLongUnterminatedStringReader() throws IOException {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[" + string;
    JsonReader reader = new JsonReader(reader(json));
    reader.setLenient(true);
    reader.beginArray();

    Reader stringReader = reader.nextStringReader();
    char[] readChars = new char[stringChars.length];
    while (stringReader.read(readChars) != -1) { }
    stringReader.close();
    assertArrayEquals(stringChars, readChars);

    try {
      reader.peek();
      fail();
    } catch (EOFException expected) {
    }
  }

  public void testSkipVeryLongUnquotedString() throws IOException {
    JsonReader reader = new JsonReader(reader("[" + repeat('x', 8192) + "]"));
    reader.setLenient(true);
    reader.beginArray();
    reader.skipValue();
    reader.endArray();
  }

  public void testSkipTopLevelUnquotedString() throws IOException {
    JsonReader reader = new JsonReader(reader(repeat('x', 8192)));
    reader.setLenient(true);
    reader.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipVeryLongQuotedString() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"" + repeat('x', 8192) + "\"]"));
    reader.beginArray();
    reader.skipValue();
    reader.endArray();
  }

  public void testSkipTopLevelQuotedString() throws IOException {
    JsonReader reader = new JsonReader(reader("\"" + repeat('x', 8192) + "\""));
    reader.setLenient(true);
    reader.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testStringAsNumberWithTruncatedExponent() throws IOException {
    JsonReader reader = new JsonReader(reader("[123e]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(STRING, reader.peek());
  }

  public void testStringAsNumberWithDigitAndNonDigitExponent() throws IOException {
    JsonReader reader = new JsonReader(reader("[123e4b]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(STRING, reader.peek());
  }

  public void testStringAsNumberWithNonDigitExponent() throws IOException {
    JsonReader reader = new JsonReader(reader("[123eb]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(STRING, reader.peek());
  }

  public void testEmptyStringName() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"\":true}"));
    reader.setLenient(true);
    assertEquals(BEGIN_OBJECT, reader.peek());
    reader.beginObject();
    assertEquals(NAME, reader.peek());
    assertEquals("", reader.nextName());
    assertEquals(JsonToken.BOOLEAN, reader.peek());
    assertEquals(true, reader.nextBoolean());
    assertEquals(JsonToken.END_OBJECT, reader.peek());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testEmptyStringNameReader() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"\":true}"));
    reader.setLenient(true);
    assertEquals(BEGIN_OBJECT, reader.peek());
    reader.beginObject();
    assertEquals(NAME, reader.peek());

    Reader nameReader = reader.nextNameReader();
    assertEquals(-1, nameReader.read());
    nameReader.close();

    assertEquals(JsonToken.BOOLEAN, reader.peek());
    assertEquals(true, reader.nextBoolean());
    assertEquals(JsonToken.END_OBJECT, reader.peek());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testStrictExtraCommasInMaps() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":\"b\",}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals("b", reader.nextString());
    try {
      reader.peek();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientExtraCommasInMaps() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":\"b\",}"));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals("b", reader.nextString());
    try {
      reader.peek();
      fail();
    } catch (IOException expected) {
    }
  }

  private static String repeat(char c, int count) {
    char[] array = new char[count];
    Arrays.fill(array, c);
    return new String(array);
  }

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
  public void testUnterminatedStringFailure() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"string"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testUnterminatedStringFailureReader() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"string"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
    Reader stringReader = reader.nextStringReader();
    try {
      while (stringReader.read() != -1) { }
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testActiveStringReaderException() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"a\"]"));
    reader.beginArray();
    reader.nextStringReader();

    // The following actions should fail because the stringReader has not
    // consumed all chars yet
    try {
      reader.peek();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.skipValue();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.hasNext();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  public void testActiveNameReaderException() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\": 1}"));
    reader.beginObject();
    reader.nextNameReader();

    // The following actions should fail because the nameReader has not
    // consumed all chars yet
    try {
      reader.peek();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.skipValue();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.hasNext();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  private static class BlockingDetectingReader extends Reader {
    private CharSequence str;
    private int nextCharIndex;

    private BlockingDetectingReader(CharSequence str) {
      this.str = str;
      this.nextCharIndex = 0;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      if (len == 0) {
        return 0;
      }

      int readAmount = Math.min(len, str.length() - nextCharIndex);
      if (readAmount == 0) {
        fail("Reader would block");
      }
      else {
        for (int i = 0; i < readAmount; i++) {
          cbuf[off + i] = str.charAt(nextCharIndex);
          nextCharIndex++;
        }
      }

      return readAmount;
    }

    @Override
    public boolean ready() throws IOException {
      return str.length() - nextCharIndex > 0;
    }

    @Override
    public void close() throws IOException {
    }
  }

  public void testStringReader() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"abc\"]"));
    reader.beginArray();

    Reader stringReader = reader.nextStringReader();
    try {
      stringReader.skip(-1); // Skipping negative amount of chars should fail
      fail();
    } catch (IllegalArgumentException expected) {
    }
    assertEquals(0, stringReader.skip(0)); // Skipping 0 chars should have no effect
    assertEquals(1, stringReader.skip(1));
    assertEquals('b', stringReader.read());
    // Trying to skip more than the remaining chars should only skip remaining
    assertEquals(1, stringReader.skip(100));
    assertEquals(-1, stringReader.read());
    stringReader.close();

    assertEquals("$[1]", reader.getPath());
    assertEquals(JsonToken.END_ARRAY, reader.peek());
  }

  public void testStringReaderReady() throws IOException {
    // String `a\` = unfinished escape sequence
    JsonReader reader = new JsonReader(new BlockingDetectingReader("[\"a\\"));
    reader.beginArray();

    Reader stringReader = reader.nextStringReader();
    assertTrue(stringReader.ready());
    assertEquals('a', stringReader.read());
    assertFalse(stringReader.ready()); // Underlying reader would block now
  }

  /**
   * Tests {@link StringValueReader} {@code mark} related methods.
   * String value must be {@code "abcd"}.
   */
  private static void testStringValueReaderMark(StringValueReader reader) throws IOException {
    assertFalse(reader.markSupported());

    try {
      reader.mark(2);
      fail();
    } catch (IOException expected) {
      assertEquals("mark() not supported", expected.getMessage());
    }
    try {
      reader.reset();
      fail();
    } catch (IOException expected) {
      assertEquals("reset() not supported", expected.getMessage());
    }

    // Make sure that `mark` methods had no effect
    assertEquals('a', reader.read());
  }

  public void testStringReaderMark() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"abcd\"]"));
    reader.beginArray();
    StringValueReader stringReader = reader.nextStringReader();
    testStringValueReaderMark(stringReader);
  }

  /**
   * Returns the content which has been {@code put(...)} into the buffer,
   * i.e. everything from absolute index 0 to (excluding) {@code position()}.
   */
  private static String getPutCharBufferContent(CharBuffer charBuffer) {
    int length = charBuffer.position();
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(charBuffer.get(i));
    }
    return sb.toString();
  }

  public void testStringReaderReadToCharBuffer() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"abcd\"]"));
    reader.beginArray();
    Reader stringReader = reader.nextStringReader();
    try {
      // Reading into full CharBuffer should check for read-only anyways
      stringReader.read(CharBuffer.allocate(0).asReadOnlyBuffer());
      fail();
    } catch (ReadOnlyBufferException expected) {
    }

    CharBuffer charBuffer = CharBuffer.allocate(10);
    // Put something in buffer to make sure it is not overwritten
    charBuffer.put('t');
    charBuffer.limit(3);
    assertEquals(2, stringReader.read(charBuffer));
    assertEquals("tab", getPutCharBufferContent(charBuffer));
    assertEquals(3, charBuffer.position());

    // Read remaining chars
    charBuffer.limit(10);
    assertEquals(2, stringReader.read(charBuffer));
    assertEquals("tabcd", getPutCharBufferContent(charBuffer));
    assertEquals(5, charBuffer.position());

    // End of stream should return -1
    assertEquals(-1, stringReader.read(charBuffer));
    // Trying to read into full CharBuffer should return 0, even at end of stream
    assertEquals(0, stringReader.read(CharBuffer.allocate(0)));
    stringReader.close();

    // Make sure nothing changed after trying to read at end of stream
    assertEquals("tabcd", getPutCharBufferContent(charBuffer));
    assertEquals(5, charBuffer.position());

    assertEquals(JsonToken.END_ARRAY, reader.peek());
  }

  /**
   * {@link JsonReader#peek()} parses numbers completely, so {@code nextStringReader()}
   * has to treat them differently
   */
  public void testStringReaderNumbers() throws IOException {
    List<String> numbers = Arrays.asList("1234", "1234.12312341");

    for (String number : numbers) {
      JsonReader reader = new JsonReader(reader("[" + number + "]"));
      reader.beginArray();
      Reader stringReader = reader.nextStringReader();
      char[] buf = new char[number.length()];
      stringReader.read(buf);
      assertArrayEquals(number.toCharArray(), buf);
      assertEquals(-1, stringReader.read());
    }
  }

  /**
   * Tests {@link StringValueReader#readAtLeast(char[], int, int, int)}.
   * String value must be {@code "abcd"}.
   */
  private static void testStringValueReaderReadAtLeast(StringValueReader reader) throws IOException {
    char[] buf = new char[3];
    // Try all kinds of invalid arguments
    try {
      // Invalid off
      reader.readAtLeast(buf, -1, 0, 1);
      fail();
    } catch (IndexOutOfBoundsException expected) {
    }
    try {
      // Invalid minLen
      reader.readAtLeast(buf, 0, -1, 1);
      fail();
    } catch (IndexOutOfBoundsException expected) {
    }
    try {
      // minLen > maxLen
      reader.readAtLeast(buf, 0, 2, 1);
      fail();
    } catch (IndexOutOfBoundsException expected) {
    }
    try {
      // maxLen > cbuf.length - off
      reader.readAtLeast(buf, 0, 1, buf.length + 1);
      fail();
    } catch (IndexOutOfBoundsException expected) {
    }

    // Trying to read 0 chars should have no effect
    int read = reader.readAtLeast(buf, 0, 0, 0);
    assertEquals(0, read);

    int minRead = 2;
    read = reader.readAtLeast(buf, 0, minRead, minRead + 1);
    assertTrue(read >= minRead);
    char nextExpected;
    if (read == 2) { // Don't test against implementation; allow either 2 or 3
      assertArrayEquals(new char[] {'a', 'b', 0}, buf);
      nextExpected = 'c';
    } else {
      assertArrayEquals(new char[] {'a', 'b', 'c'}, buf);
      nextExpected = 'd';
    }

    // Should always try to read at least one char even if minRead = 0
    minRead = 0;
    read = reader.readAtLeast(buf, 2, minRead, minRead + 1);
    assertEquals(1, read);
    assertEquals(nextExpected, buf[2]);

    // Skip remaining chars
    while (reader.read() != -1) { }

    // Trying to read at least 1 char when end has been reached should throw EOF
    try {
      reader.readAtLeast(buf, 0, 1, 2);
      fail();
    } catch (EOFException expected) {
    }

    // Trying to read at least 0 char when end has been reached should return 0
    read = reader.readAtLeast(buf, 0, 0, 2);
    assertEquals(0, read);
  }

  public void testStringReaderReadAtLeast() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"abcd\"]"));
    reader.beginArray();

    StringValueReader stringReader = reader.nextStringReader();
    testStringValueReaderReadAtLeast(stringReader);
    stringReader.close();

    reader.endArray();
  }

  /**
   * Tests {@link StringValueReader#readGreedily(char[], int, int)}.
   * String value must be {@code "abcd"}.
   */
  private static void testStringValueReaderReadGreedily(StringValueReader reader) throws IOException {
    char[] buf = new char[4];
    // Try all kinds of invalid arguments
    try {
      // Invalid off
      reader.readGreedily(buf, -1, 1);
      fail();
    } catch (IndexOutOfBoundsException expected) {
    }
    try {
      // Invalid len
      reader.readGreedily(buf, 0, -1);
      fail();
    } catch (IndexOutOfBoundsException expected) {
    }
    try {
      // len > cbuf.length - off
      reader.readGreedily(buf, 0, buf.length + 1);
      fail();
    } catch (IndexOutOfBoundsException expected) {
    }

    // Trying to read 0 chars should have no effect
    assertEquals(0, reader.readGreedily(buf, 0, 0));

    int expectedRead = 2;
    int read = reader.readGreedily(buf, 0, expectedRead);
    assertEquals(expectedRead, read);
    assertArrayEquals(new char[] {'a', 'b', '\0', '\0'}, buf);

    read = reader.readGreedily(buf, 1, 3);
    assertEquals(2, read);
    assertArrayEquals(new char[] {'a', 'c', 'd', '\0'}, buf);

    assertEquals(-1, reader.read());
    assertEquals(-1,  reader.readGreedily(buf, 0, 2));

    // Trying to read 0 chars when end has been reached should return 0
    assertEquals(0, reader.readGreedily(buf, 0, 0));
  }

  public void testStringReaderReadGreedily() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"abcd\"]"));
    reader.beginArray();

    StringValueReader stringReader = reader.nextStringReader();
    testStringValueReaderReadGreedily(stringReader);
    stringReader.close();

    reader.endArray();
  }

  /**
   * Tests against the implementation of {@link StringValueReaderImpl} and
   * {@link JsonReader#fillBufferForEscapeCharacter()} to make sure an escape
   * sequence is read in case the reader does not block.
   */
  public void testStringValueReaderEscapeSequence() throws IOException {
    final int MIN_DESIRED_ACCEPT = StringValueReaderImpl.MIN_DESIRED_ACCEPT;
    // Char indicating that reader ready() should return false for this char
    final char blockingChar = '#';

    String expectedName;
    String expectedValue;
    final String json;
    final int[] portionLengths;

    // Setup test:
    {
      String jsonName = repeat('a', MIN_DESIRED_ACCEPT) + "\\u1234" + blockingChar;
      expectedName = repeat('a', MIN_DESIRED_ACCEPT) + "\u1234";
      String jsonValue = repeat('b', MIN_DESIRED_ACCEPT) + "\\u2345" + blockingChar;
      expectedValue = repeat('b', MIN_DESIRED_ACCEPT) + "\u2345";

      String jsonPrefix = "{\"";
      String nameValueSeparator = "\":\"";
      String jsonSuffix = "\"}";
      json = jsonPrefix + jsonName + nameValueSeparator + jsonValue + jsonSuffix;

      portionLengths = new int[] {
        jsonPrefix.length(),
        // name; provide `a...a\` and then unicode escape chars + blockingChar
        MIN_DESIRED_ACCEPT + 1, 1, 1, 1, 1, 1, 1,
        nameValueSeparator.length(),
        // value; provide `b...b\` and then unicode escape chars + blockingChar
        MIN_DESIRED_ACCEPT + 1, 1, 1, 1, 1, 1, 1,
        jsonSuffix.length(),
        0
      };

      // Validate that test is working as expected
      int sum = 0;
      for (int portionLength : portionLengths) {
        sum += portionLength;
      }
      assertEquals(json.length(), sum);
    }

    final AtomicBoolean checkedReady = new AtomicBoolean(false);
    class PortionedReader extends Reader {
      /** Index within {@code json} */
      private int index = 0;
      private int portionIndex = 0;
      private int portionRemaining = portionLengths[portionIndex];

      @Override
      public int read(char[] cbuf, int off, int len) throws IOException {
        if (portionRemaining <= 0) {
          return -1;
        }

        int readAmount = Math.min(len, portionRemaining);
        portionRemaining -= readAmount;
        json.getChars(index, index + readAmount, cbuf, off);
        index += readAmount;
        if (portionRemaining <= 0) {
          portionRemaining = portionLengths[++portionIndex];
        }
        return readAmount;
      }

      @Override
      public boolean ready() throws IOException {
        checkedReady.set(true);
        return index >= json.length() || json.charAt(index) != blockingChar;
      }

      @Override
      public void close() throws IOException {
      }
    }

    JsonReader reader = new JsonReader(new PortionedReader());
    reader.beginObject();

    StringValueReader nameReader = reader.nextNameReader();
    char[] nameBuf = new char[expectedName.length() + 1]; // + 1 for blockingChar
    // StringValueReader implementation should have filled complete buffer
    // except last char because reader indicated that it won't block
    nameReader.readAtLeast(nameBuf, 0, 0, nameBuf.length);
    assertTrue(checkedReady.get());
    assertEquals(expectedName + '\0', new String(nameBuf));
    assertEquals(blockingChar, nameReader.read());
    assertEquals(-1, nameReader.read());
    nameReader.close();

    StringValueReader stringReader = reader.nextStringReader();
    char[] valueBuf = new char[expectedValue.length() + 1]; // + 1 for blockingChar
    // StringValueReader implementation should have filled complete buffer
    // except last char because reader indicated that it won't block
    stringReader.readAtLeast(valueBuf, 0, 0, valueBuf.length);
    assertEquals(expectedValue + '\0', new String(valueBuf));
    assertEquals(blockingChar, stringReader.read());
    assertEquals(-1, stringReader.read());
    stringReader.close();

    reader.endObject();
  }

  /**
   * Tests {@link StringValueReader#skipExactly(long)}.
   * String value must be {@code "abcd"}.
   */
  private static void testStringValueReaderSkipExactly(StringValueReader reader) throws IOException {
    try {
      reader.skipExactly(-1);
      fail();
    } catch (IllegalArgumentException expected) {
    }

    reader.skipExactly(2);
    assertEquals('c', reader.read());
    reader.skipExactly(0); // should have no effect
    assertEquals('d', reader.read());

    // Skip remaining chars
    while (reader.read() != -1) { }

    try {
      reader.skipExactly(1);
      fail();
    } catch (EOFException expected) {
    }
    // However, trying to skip 0 chars should work
    reader.skipExactly(0);
  }

  public void testStringReaderSkipExactly() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"abcd\"]"));
    reader.beginArray();

    StringValueReader stringReader = reader.nextStringReader();
    testStringValueReaderSkipExactly(stringReader);
    stringReader.close();

    reader.endArray();
  }

  /**
   * Tests {@link StringValueReader#skipRemaining()}.
   */
  private static void testStringValueReaderSkipRemaining(StringValueReader reader) throws IOException {
    reader.skipRemaining();
    // Should have reached end
    assertEquals(-1, reader.read());
    // Should have no effect
    reader.skipRemaining();
  }

  public void testStringReaderSkipRemaining() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"abcd\",\"efgh\"]"));
    reader.beginArray();

    StringValueReader stringReader = reader.nextStringReader();
    testStringValueReaderSkipRemaining(stringReader);
    stringReader.close();

    // Verify that skipRemaining on its own actually reaches end, i.e. after close()
    // JsonReader can read next token
    stringReader = reader.nextStringReader();
    stringReader.skipRemaining();
    stringReader.close();

    reader.endArray();
  }

  /**
   * Tests how the {@link StringValueReader} methods behave on a closed reader.
   * The provided reader must already be closed.
   */
  private static void testStringValueReaderClosed(StringValueReader reader) throws IOException {
    try {
      reader.read();
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      reader.read(new char[1]);
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      // Should also fail when trying to read into full array
      reader.read(new char[0]);
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      reader.read(CharBuffer.allocate(2));
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      // Should also fail when trying to read into full buffer
      reader.read(CharBuffer.allocate(0));
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      reader.readAtLeast(new char[2], 0, 1, 2);
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      // Should also fail when trying to read 0 chars
      reader.readAtLeast(new char[2], 0, 0, 0);
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      reader.readGreedily(new char[2], 0, 2);
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      // Should also fail when trying to read 0 chars
      reader.readGreedily(new char[2], 0, 0);
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      reader.read(new char[2], 0, 2);
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      // Should also fail when trying to read 0 chars
      reader.read(new char[2], 0, 0);
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      reader.ready();
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      reader.skip(1);
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      // Should also fail when trying to skip 0 chars
      reader.skip(0);
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      reader.skipExactly(1);
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      // Should also fail when trying to skip 0 chars
      reader.skipExactly(0);
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }
    try {
      reader.skipRemaining();
      fail();
    } catch (IOException expected) {
      assertEquals("Reader is closed", expected.getMessage());
    }

    // Closing again should have no effect
    reader.close();
  }

  /**
   * Test behavior of JsonReader after {@code nextStringReader()} has
   * been closed before all data has been consumed
   */
  public void testStringReaderClosePartial() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"abcd\"]"));
    reader.beginArray();

    StringValueReader partialStringReader = reader.nextStringReader();
    char[] buf = new char[1];
    partialStringReader.read(buf);
    assertArrayEquals(new char[] {'a'}, buf);
    partialStringReader.close();
    testStringValueReaderClosed(partialStringReader);

    // JsonReader should still prevent further actions because
    // string reader did not consume all data
    try {
      reader.peek();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  public void testStringReaderClose() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"abcd\"]"));
    reader.beginArray();

    StringValueReader stringReader = reader.nextStringReader();
    // Read complete string
    while (stringReader.read() != -1) { }
    stringReader.close();
    testStringValueReaderClosed(stringReader);

    reader.endArray();
  }

  public void testNameReader() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"abc\": 1}"));
    reader.beginObject();

    Reader nameReader = reader.nextNameReader();
    try {
      nameReader.skip(-1); // Skipping negative amount of chars should fail
      fail();
    } catch (IllegalArgumentException expected) {
    }
    assertEquals(0, nameReader.skip(0)); // Skipping 0 chars should have no effect
    assertEquals(1, nameReader.skip(1));
    assertEquals('b', nameReader.read());
    // Trying to skip more than the remaining chars should only skip remaining
    assertEquals(1, nameReader.skip(100));
    assertEquals(-1, nameReader.read());
    nameReader.close();

    assertEquals("$.#streamedName", reader.getPath());

    assertEquals(JsonToken.NUMBER, reader.peek());
  }

  public void testNameReaderReady() throws IOException {
    // Name `a\` = unfinished escape sequence
    JsonReader reader = new JsonReader(new BlockingDetectingReader("{\"a\\"));
    reader.beginObject();

    Reader nameReader = reader.nextNameReader();
    assertTrue(nameReader.ready());
    assertEquals('a', nameReader.read());
    assertFalse(nameReader.ready()); // Underlying reader would block now
  }

  public void testNameReaderMark() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"abcd\":1}"));
    reader.beginObject();
    StringValueReader nameReader = reader.nextNameReader();
    testStringValueReaderMark(nameReader);
  }

  public void testNameReaderReadAtLeast() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"abcd\":1}"));
    reader.beginObject();

    StringValueReader nameReader = reader.nextNameReader();
    testStringValueReaderReadAtLeast(nameReader);
    nameReader.close();

    assertEquals(1, reader.nextInt());
    reader.endObject();
  }

  public void testNameReaderReadGreedily() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"abcd\":1}"));
    reader.beginObject();

    StringValueReader nameReader = reader.nextNameReader();
    testStringValueReaderReadGreedily(nameReader);
    nameReader.close();

    assertEquals(1, reader.nextInt());
    reader.endObject();
  }

  public void testNameReaderSkipExactly() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"abcd\":1}"));
    reader.beginObject();

    StringValueReader nameReader = reader.nextNameReader();
    testStringValueReaderSkipExactly(nameReader);
    nameReader.close();

    assertEquals(1, reader.nextInt());
    reader.endObject();
  }

  public void testNameReaderSkipRemaining() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"abcd\":1,\"efgh\":2}"));
    reader.beginObject();

    StringValueReader nameReader = reader.nextNameReader();
    testStringValueReaderSkipRemaining(nameReader);
    nameReader.close();
    assertEquals(1, reader.nextInt());

    // Verify that skipRemaining on its own actually reaches end, i.e. after close()
    // JsonReader can read next token
    nameReader = reader.nextNameReader();
    nameReader.skipRemaining();
    nameReader.close();
    assertEquals(2, reader.nextInt());

    reader.endObject();
  }

  /**
   * Test behavior of JsonReader after {@code nextNameReader()} has
   * been closed before all data has been consumed
   */
  public void testNameReaderClosePartial() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"abcd\":1}"));
    reader.beginObject();

    StringValueReader partialNameReader = reader.nextNameReader();
    char[] buf = new char[1];
    partialNameReader.read(buf);
    assertArrayEquals(new char[] {'a'}, buf);
    partialNameReader.close();
    testStringValueReaderClosed(partialNameReader);

    // JsonReader should still prevent further actions because
    // name reader did not consume all data
    try {
      reader.peek();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  public void testNameReaderClose() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"abcd\":1}"));
    reader.beginObject();

    StringValueReader nameReader = reader.nextNameReader();
    // Read complete name
    while (nameReader.read() != -1) { }
    nameReader.close();
    testStringValueReaderClosed(nameReader);

    assertEquals(1, reader.nextInt());
    reader.endObject();
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
        assertEquals("name", reader.nextName());
      } else if (expectation == BOOLEAN) {
        assertEquals(false, reader.nextBoolean());
      } else if (expectation == STRING) {
        assertEquals("string", reader.nextString());
      } else if (expectation == NUMBER) {
        assertEquals(123, reader.nextInt());
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
