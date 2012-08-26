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

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import junit.framework.TestCase;

public final class JsonReaderTest extends TestCase {

  public void testReadArray() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[true, true]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    assertEquals(true, reader.nextBoolean());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testReadEmptyArray() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[]"));
    reader.beginArray();
    assertFalse(reader.hasNext());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testReadObject() throws IOException {
    JsonReader reader = new JsonReader(new StringReader(
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
    JsonReader reader = new JsonReader(new StringReader("{}"));
    reader.beginObject();
    assertFalse(reader.hasNext());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipObject() throws IOException {
    JsonReader reader = new JsonReader(new StringReader(
        "{\"a\": { \"c\": [], \"d\": [true, true, {}] }, \"b\": \"banana\"}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    reader.skipValue();
    assertEquals("b", reader.nextName());
    reader.skipValue();
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testSkipInteger() throws IOException {
    JsonReader reader = new JsonReader(new StringReader(
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
    JsonReader reader = new JsonReader(new StringReader(
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
    JsonReader reader = new JsonReader(new StringReader(json));
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

  public void testNulls() {
    try {
      new JsonReader(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testEmptyString() {
    try {
      new JsonReader(new StringReader("")).beginArray();
    } catch (IOException expected) {
    }
    try {
      new JsonReader(new StringReader("")).beginObject();
    } catch (IOException expected) {
    }
  }

  public void testNoTopLevelObject() {
    try {
      new JsonReader(new StringReader("true")).nextBoolean();
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
    JsonReader reader = new JsonReader(new StringReader(json));
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
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (NumberFormatException expected) {
    }
  }

  public void testUnescapingTruncatedCharacters() throws IOException {
    String json = "[\"\\u000";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testUnescapingTruncatedSequence() throws IOException {
    String json = "[\"\\";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testIntegersWithFractionalPartSpecified() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1.0,1.0,1.0]"));
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
    JsonReader reader = new JsonReader(new StringReader(json));
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
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    try {
      reader.nextDouble();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStrictQuotedNonFiniteDoubles() throws IOException {
    String json = "[\"NaN\"]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    try {
      reader.nextDouble();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientNonFiniteDoubles() throws IOException {
    String json = "[NaN, -Infinity, Infinity]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginArray();
    assertTrue(Double.isNaN(reader.nextDouble()));
    assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble());
    assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble());
    reader.endArray();
  }

  public void testLenientQuotedNonFiniteDoubles() throws IOException {
    String json = "[\"NaN\", \"-Infinity\", \"Infinity\"]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginArray();
    assertTrue(Double.isNaN(reader.nextDouble()));
    assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble());
    assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble());
    reader.endArray();
  }

  public void testStrictNonFiniteDoublesWithSkipValue() throws IOException {
    String json = "[NaN]";
    JsonReader reader = new JsonReader(new StringReader(json));
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
    JsonReader reader = new JsonReader(new StringReader(json));
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

  /**
   * This test fails because there's no double for 9223372036854775806, and
   * our long parsing uses Double.parseDouble() for fractional values.
   */
  public void disabled_testHighPrecisionLong() throws IOException {
    String json = "[9223372036854775806.000]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    assertEquals(9223372036854775806L, reader.nextLong());
    reader.endArray();
  }

  public void testNumberWithOctalPrefix() throws IOException {
    String json = "[01]";
    JsonReader reader = new JsonReader(new StringReader(json));
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
    JsonReader reader = new JsonReader(new StringReader("[true,false]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    assertEquals(false, reader.nextBoolean());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testPeekingUnquotedStringsPrefixedWithBooleans() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[truey]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
    try {
      reader.nextBoolean();
      fail();
    } catch (IllegalStateException expected) {
    }
    assertEquals("truey", reader.nextString());
    reader.endArray();
  }

  public void testPeekingUnquotedStringsPrefixedWithIntegers() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[12.34e5x]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
    try {
      reader.nextInt();
      fail();
    } catch (IllegalStateException expected) {
    }
    assertEquals("12.34e5x", reader.nextString());
  }

  public void testPeekLongMinValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[-9223372036854775808]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.NUMBER, reader.peek());
    assertEquals(-9223372036854775808L, reader.nextLong());
  }

  /**
   * This test fails because there's no double for -9223372036854775809, and our
   * long parsing uses Double.parseDouble() for fractional values.
   */
  public void testPeekLargerThanLongMinValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[-9223372036854775809]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.NUMBER, reader.peek());
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertEquals(-9223372036854775809d, reader.nextDouble());
  }

  public void testPeekMuchLargerThanLongMinValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[-92233720368547758080]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.NUMBER, reader.peek());
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
    assertEquals(-92233720368547758080d, reader.nextDouble());
  }

  public void testQuotedNumberWithEscape() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"12\u00334\"]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals(1234, reader.nextInt());
  }

  public void testMixedCaseLiterals() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[True,TruE,False,FALSE,NULL,nulL]"));
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
    JsonReader reader = new JsonReader(new StringReader("{\"a\":}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.nextString();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testPrematureEndOfInput() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\":true,"));
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
      JsonReader reader = new JsonReader(new StringReader("{\"a\":[]}"));
      reader.beginObject();
      reader.close();
      reader.nextName();
      fail();
    } catch (IllegalStateException expected) {
    }

    try {
      JsonReader reader = new JsonReader(new StringReader("{\"a\":[]}"));
      reader.close();
      reader.beginObject();
      fail();
    } catch (IllegalStateException expected) {
    }

    try {
      JsonReader reader = new JsonReader(new StringReader("{\"a\":true}"));
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
    JsonReader reader = new JsonReader(new StringReader("{\"a\":true}"));
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
    JsonReader reader = new JsonReader(new StringReader("[1.5]"));
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
    JsonReader reader = new JsonReader(new StringReader("[\"null\"]"));
    reader.beginArray();
    try {
      reader.nextNull();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  public void testNullLiteralIsNotAString() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[null]"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  public void testStrictNameValueSeparator() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\"=true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(new StringReader("{\"a\"=>true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientNameValueSeparator() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\"=true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(true, reader.nextBoolean());

    reader = new JsonReader(new StringReader("{\"a\"=>true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(true, reader.nextBoolean());
  }

  public void testStrictNameValueSeparatorWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\"=true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(new StringReader("{\"a\"=>true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictComments() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[// comment \n true]"));
    reader.beginArray();
    try {
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(new StringReader("[# comment \n true]"));
    reader.beginArray();
    try {
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(new StringReader("[/* comment */ true]"));
    reader.beginArray();
    try {
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientComments() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[// comment \n true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());

    reader = new JsonReader(new StringReader("[# comment \n true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());

    reader = new JsonReader(new StringReader("[/* comment */ true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
  }

  public void testStrictCommentsWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[// comment \n true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(new StringReader("[# comment \n true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(new StringReader("[/* comment */ true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictUnquotedNames() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{a:true}"));
    reader.beginObject();
    try {
      reader.nextName();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientUnquotedNames() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{a:true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
  }

  public void testStrictUnquotedNamesWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{a:true}"));
    reader.beginObject();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictSingleQuotedNames() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{'a':true}"));
    reader.beginObject();
    try {
      reader.nextName();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientSingleQuotedNames() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{'a':true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
  }

  public void testStrictSingleQuotedNamesWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{'a':true}"));
    reader.beginObject();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictUnquotedStrings() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[a]"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStrictUnquotedStringsWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[a]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLenientUnquotedStrings() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[a]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals("a", reader.nextString());
  }

  public void testStrictSingleQuotedStrings() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("['a']"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientSingleQuotedStrings() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("['a']"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals("a", reader.nextString());
  }

  public void testStrictSingleQuotedStringsWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("['a']"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictSemicolonDelimitedArray() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[true;true]"));
    reader.beginArray();
    try {
      reader.nextBoolean();
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientSemicolonDelimitedArray() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[true;true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    assertEquals(true, reader.nextBoolean());
  }

  public void testStrictSemicolonDelimitedArrayWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[true;true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictSemicolonDelimitedNameValuePair() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\":true;\"b\":true}"));
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
    JsonReader reader = new JsonReader(new StringReader("{\"a\":true;\"b\":true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(true, reader.nextBoolean());
    assertEquals("b", reader.nextName());
  }

  public void testStrictSemicolonDelimitedNameValuePairWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\":true;\"b\":true}"));
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
    JsonReader reader = new JsonReader(new StringReader("[true,,true]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    try {
      reader.nextNull();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(new StringReader("[,true]"));
    reader.beginArray();
    try {
      reader.nextNull();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(new StringReader("[true,]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    try {
      reader.nextNull();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(new StringReader("[,]"));
    reader.beginArray();
    try {
      reader.nextNull();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientUnnecessaryArraySeparators() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[true,,true]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    reader.nextNull();
    assertEquals(true, reader.nextBoolean());
    reader.endArray();

    reader = new JsonReader(new StringReader("[,true]"));
    reader.setLenient(true);
    reader.beginArray();
    reader.nextNull();
    assertEquals(true, reader.nextBoolean());
    reader.endArray();

    reader = new JsonReader(new StringReader("[true,]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    reader.nextNull();
    reader.endArray();

    reader = new JsonReader(new StringReader("[,]"));
    reader.setLenient(true);
    reader.beginArray();
    reader.nextNull();
    reader.nextNull();
    reader.endArray();
  }

  public void testStrictUnnecessaryArraySeparatorsWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[true,,true]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(new StringReader("[,true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(new StringReader("[true,]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }

    reader = new JsonReader(new StringReader("[,]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictMultipleTopLevelValues() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[] []"));
    reader.beginArray();
    reader.endArray();
    try {
      reader.peek();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientMultipleTopLevelValues() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[] true {}"));
    reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertEquals(true, reader.nextBoolean());
    reader.beginObject();
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testStrictMultipleTopLevelValuesWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[] []"));
    reader.beginArray();
    reader.endArray();
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictTopLevelString() {
    JsonReader reader = new JsonReader(new StringReader("\"a\""));
    try {
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientTopLevelString() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\"a\""));
    reader.setLenient(true);
    assertEquals("a", reader.nextString());
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testStrictTopLevelValueType() {
    JsonReader reader = new JsonReader(new StringReader("true"));
    try {
      reader.nextBoolean();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientTopLevelValueType() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("true"));
    reader.setLenient(true);
    assertEquals(true, reader.nextBoolean());
  }

  public void testStrictTopLevelValueTypeWithSkipValue() {
    JsonReader reader = new JsonReader(new StringReader("true"));
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictNonExecutePrefix() {
    JsonReader reader = new JsonReader(new StringReader(")]}'\n []"));
    try {
      reader.beginArray();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testStrictNonExecutePrefixWithSkipValue() {
    JsonReader reader = new JsonReader(new StringReader(")]}'\n []"));
    try {
      reader.skipValue();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testLenientNonExecutePrefix() throws IOException {
    JsonReader reader = new JsonReader(new StringReader(")]}'\n []"));
    reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testLenientNonExecutePrefixWithLeadingWhitespace() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\r\n \t)]}'\n []"));
    reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testLenientPartialNonExecutePrefix() {
    JsonReader reader = new JsonReader(new StringReader(")]}' []"));
    reader.setLenient(true);
    try {
      assertEquals(")", reader.nextString());
      reader.nextString();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testBomIgnoredAsFirstCharacterOfDocument() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\ufeff[]"));
    reader.beginArray();
    reader.endArray();
  }

  public void testBomForbiddenAsOtherCharacterInDocument() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\ufeff]"));
    reader.beginArray();
    try {
      reader.endArray();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testFailWithPosition() throws IOException {
    testFailWithPosition("Expected value at line 6 column 3",
        "[\n\n\n\n\n0,}]");
  }

  public void testFailWithPositionGreaterThanBufferSize() throws IOException {
    String spaces = repeat(' ', 8192);
    testFailWithPosition("Expected value at line 6 column 3",
        "[\n\n" + spaces + "\n\n\n0,}]");
  }

  public void testFailWithPositionIsOffsetByBom() throws IOException {
    testFailWithPosition("Expected value at line 1 column 4",
        "\ufeff[0,}]");
  }
  
  private void testFailWithPosition(String message, String json) throws IOException {
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    reader.nextInt();
    try {
      reader.peek();
      fail();
    } catch (IOException expected) {
      assertEquals(message, expected.getMessage());
    }
  }
  
  public void testDeeplyNestedArrays() throws IOException {
    // this is nested 40 levels deep; Gson is tuned for nesting is 30 levels deep or fewer
    JsonReader reader = new JsonReader(new StringReader(
        "[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]"));
    for (int i = 0; i < 40; i++) {
      reader.beginArray();
    }
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

    JsonReader reader = new JsonReader(new StringReader(json));
    for (int i = 0; i < 40; i++) {
      reader.beginObject();
      assertEquals("a", reader.nextName());
    }
    assertEquals(true, reader.nextBoolean());
    for (int i = 0; i < 40; i++) {
      reader.endObject();
    }
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  // http://code.google.com/p/google-gson/issues/detail?id=409
  public void testStringEndingInSlash() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("/"));
    reader.setLenient(true);
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testDocumentWithCommentEndingInSlash() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("/* foo *//"));
    reader.setLenient(true);
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testStringWithLeadingSlash() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("/x"));
    reader.setLenient(true);
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testUnterminatedObject() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\":\"android\"x"));
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
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    assertEquals(string, reader.nextString());
    reader.endArray();
  }

  public void testVeryLongUnquotedString() throws IOException {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[" + string + "]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(string, reader.nextString());
    reader.endArray();
  }

  public void testVeryLongUnterminatedString() throws IOException {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[" + string;
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(string, reader.nextString());
    try {
      reader.peek();
      fail();
    } catch (EOFException expected) {
    }
  }

  public void testSkipVeryLongUnquotedString() throws IOException {
    char[] stringChars = new char[1024 * 16];
    Arrays.fill(stringChars, 'x');
    String string = new String(stringChars);
    String json = "[" + string + "]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginArray();
    reader.skipValue();
    reader.endArray();
  }

  public void testStringAsNumberWithTruncatedExponent() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[123e]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
  }

  public void testStringAsNumberWithDigitAndNonDigitExponent() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[123e4b]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
  }

  public void testStringAsNumberWithNonDigitExponent() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[123eb]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
  }

  public void testEmptyStringName() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"\":true}"));
    reader.setLenient(true);
    assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
    reader.beginObject();
    assertEquals(JsonToken.NAME, reader.peek());
    assertEquals("", reader.nextName());
    assertEquals(JsonToken.BOOLEAN, reader.peek());
    assertEquals(true, reader.nextBoolean());
    assertEquals(JsonToken.END_OBJECT, reader.peek());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testStrictExtraCommasInMaps() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\":\"b\",}"));
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
    JsonReader reader = new JsonReader(new StringReader("{\"a\":\"b\",}"));
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

  private String repeat(char c, int count) {
    char[] array = new char[count];
    Arrays.fill(array, c);
    return new String(array);
  }
}
