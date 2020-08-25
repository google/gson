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
import java.util.Arrays;
import junit.framework.TestCase;

import static com.google.gson.stream.JsonToken.BEGIN_ARRAY;
import static com.google.gson.stream.JsonToken.BEGIN_OBJECT;
import static com.google.gson.stream.JsonToken.BOOLEAN;
import static com.google.gson.stream.JsonToken.END_ARRAY;
import static com.google.gson.stream.JsonToken.END_DOCUMENT;
import static com.google.gson.stream.JsonToken.END_OBJECT;
import static com.google.gson.stream.JsonToken.NAME;
import static com.google.gson.stream.JsonToken.NULL;
import static com.google.gson.stream.JsonToken.NUMBER;
import static com.google.gson.stream.JsonToken.STRING;

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
    } catch (MalformedJsonException expected) {
      assertEquals("Invalid escape sequence at line 2 column 6 path $.", expected.getMessage());
    }
  }

  @SuppressWarnings("unused")
  public void testNulls() {
    try {
      new JsonReader(null);
      fail();
    } catch (NullPointerException expected) {
      assertEquals("in == null", expected.getMessage());
    }
  }

  public void testEmptyString() throws IOException {
    try {
      new JsonReader(reader("")).beginArray();
      fail();
    } catch (EOFException expected) {
      assertEquals("End of input at line 1 column 1 path $", expected.getMessage());
    }
    try {
      new JsonReader(reader("")).beginObject();
      fail();
    } catch (EOFException expected) {
      assertEquals("End of input at line 1 column 1 path $", expected.getMessage());
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
      assertEquals("Malformed unicode escape sequence \\u000g at line 1 column 3 path $[0]", expected.getMessage());
    }
  }

  public void testUnescapingTruncatedCharacters() throws IOException {
    String json = "[\"\\u000";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals("Unterminated escape sequence at line 1 column 3 path $[0]", expected.getMessage());
    }
  }

  public void testUnescapingTruncatedSequence() throws IOException {
    String json = "[\"\\";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals("Unterminated escape sequence at line 1 column 3 path $[0]", expected.getMessage());
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
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
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
      assertEquals("JSON forbids NaN and infinities: NaN at line 1 column 2 path $[0]", expected.getMessage());
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
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
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
      assertEquals("Expected an int but was -9223372036854775808 at line 1 column 23 path $[9]", expected.getMessage());
    }
    assertEquals(Long.MIN_VALUE, reader.nextLong());
    try {
      reader.nextInt();
      fail();
    } catch (NumberFormatException expected) {
      assertEquals("Expected an int but was 9223372036854775807 at line 1 column 44 path $[10]", expected.getMessage());
    }
    assertEquals(Long.MAX_VALUE, reader.nextLong());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  /**
   * Octal number notation is not supported so non-lenient JsonReader
   * should throw exception.
   */
  public void testNumberWithOctalPrefix() throws IOException {
    String json = "[012]";
    JsonReader reader = new JsonReader(reader(json));
    reader.beginArray();
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }
    try {
      reader.nextInt();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }
    try {
      reader.nextLong();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }
    try {
      reader.nextDouble();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }
  }

  /**
   * Octal number notation is not supported. In lenient mode it is
   * read as unquoted string and then {@link Integer#parseInt(String)}
   * parses it with radix 10 (simply ignoring leading 0).
   */
  public void testNumberWithOctalPrefixLenient() throws IOException {
    String json = "[012, 012, 012, 012]";
    JsonReader reader = new JsonReader(reader(json));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals("012", reader.nextString());

    // If it would be parsed as octal, decimal result would be 10
    // However, it is parsed with radix 10 so leading 0 is simply ignored
    assertEquals(12, reader.nextInt());
    assertEquals(12, reader.nextLong());
    assertEquals(12.0, reader.nextDouble());

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
      assertEquals("Expected a boolean but was STRING at line 1 column 2 path $[0]", expected.getMessage());
    }
    assertEquals("truey", reader.nextString());
    reader.endArray();
  }

  public void testMalformedNumbers() throws IOException {
    assertNotANumber("-");
    assertNotANumber(".");
    assertNotANumber("+1"); // leading `+` not allowed

    // exponent lacks digit
    assertNotANumber("e");
    assertNotANumber("0e");
    assertNotANumber(".e");
    assertNotANumber("0.e");
    assertNotANumber("-.0e");
    assertNotANumber("0e-");
    assertNotANumber("0e+");

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
      assertEquals("Value cannot be parsed as int at line 1 column 2 path $[0]", expected.getMessage());
    }
    assertEquals("12.34e5x", reader.nextString());
  }

  public void testMalformedLong() throws IOException {
    JsonReader reader = new JsonReader(reader("12xyz"));
    reader.setLenient(true);
    assertEquals(STRING, reader.peek());
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
      assertEquals("Value cannot be parsed as long at line 1 column 1 path $", expected.getMessage());
    }
  }

  public void testMalformedDouble() throws IOException {
    JsonReader reader = new JsonReader(reader("12xyz"));
    reader.setLenient(true);
    assertEquals(STRING, reader.peek());
    try {
      reader.nextDouble();
      fail();
    } catch (NumberFormatException expected) {
      assertEquals("Value cannot be parsed as double at line 1 column 1 path $", expected.getMessage());
    }
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
      assertEquals("Expected a long but was 22233720368547758070 at line 1 column 2 path $[0]", expected.getMessage());
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
      assertEquals("Expected a long but was -22233720368547758070 at line 1 column 2 path $[0]", expected.getMessage());
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
    } catch (NumberFormatException expected) {
      assertEquals("", expected.getMessage()); // Have to adjust this
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
      assertEquals("", expected.getMessage()); // Have to adjust this
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
      assertEquals("Expected a long but was -92233720368547758080 at line 1 column 2 path $[0]", expected.getMessage());
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
    } catch (MalformedJsonException expected) {
      assertEquals("Expected value at line 1 column 6 path $.a", expected.getMessage());
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
    } catch (EOFException expected) {
      assertEquals("End of input at line 1 column 11 path $.a", expected.getMessage());
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
      assertEquals("JsonReader is closed", expected.getMessage());
    }

    try {
      JsonReader reader = new JsonReader(reader("{\"a\":[]}"));
      reader.close();
      reader.beginObject();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("JsonReader is closed", expected.getMessage());
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
      assertEquals("JsonReader is closed", expected.getMessage());
    }
  }

  public void testNextFailuresDoNotAdvance() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":true}"));
    reader.beginObject();
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expected a string but was NAME at line 1 column 2 path $.", expected.getMessage());
    }
    assertEquals("a", reader.nextName());
    try {
      reader.nextName();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expected a name but was BOOLEAN at line 1 column 6 path $.a", expected.getMessage());
    }
    try {
      reader.beginArray();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expected BEGIN_ARRAY but was BOOLEAN at line 1 column 6 path $.a", expected.getMessage());
    }
    try {
      reader.endArray();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expected END_ARRAY but was BOOLEAN at line 1 column 6 path $.a", expected.getMessage());
    }
    try {
      reader.beginObject();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expected BEGIN_OBJECT but was BOOLEAN at line 1 column 6 path $.a", expected.getMessage());
    }
    try {
      reader.endObject();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expected END_OBJECT but was BOOLEAN at line 1 column 6 path $.a", expected.getMessage());
    }
    assertEquals(true, reader.nextBoolean());
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expected a string but was END_OBJECT at line 1 column 10 path $.a", expected.getMessage());
    }
    try {
      reader.nextName();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expected a name but was END_OBJECT at line 1 column 10 path $.a", expected.getMessage());
    }
    try {
      reader.beginArray();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expected BEGIN_ARRAY but was END_OBJECT at line 1 column 10 path $.a", expected.getMessage());
    }
    try {
      reader.endArray();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expected END_ARRAY but was END_OBJECT at line 1 column 10 path $.a", expected.getMessage());
    }
    reader.endObject();
    try {
      reader.endArray();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expected END_ARRAY but was END_DOCUMENT at line 1 column 11 path $", expected.getMessage());
    }
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
      assertEquals("Expected an int but was 1.5 at line 1 column 2 path $[0]", expected.getMessage());
    }
    assertEquals(1.5d, reader.nextDouble());
    reader.endArray();
  }

  public void testPeekLocationWithWrappedString() throws IOException {
    JsonReader reader = new JsonReader(reader("\"a\nb\nc\""));
    try {
      reader.nextBoolean();
    } catch (IllegalStateException expected) {
      // After peeking, reader is already in line 3, however token mismatch
      // exception message should use line number of peeked token start
      assertEquals("Expected a boolean but was STRING at line 1 column 1 path $", expected.getMessage());
    }
    assertEquals("a\nb\nc", reader.nextString());
    try {
      reader.nextBoolean();
    } catch (IllegalStateException expected) {
      assertEquals("Expected a boolean but was END_DOCUMENT at line 3 column 3 path $", expected.getMessage());
    }
  }

  public void testStringNullIsNotNull() throws IOException {
    JsonReader reader = new JsonReader(reader("[\"null\"]"));
    reader.beginArray();
    try {
      reader.nextNull();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expected null but was STRING at line 1 column 2 path $[0]", expected.getMessage());
    }
  }

  public void testNullLiteralIsNotAString() throws IOException {
    JsonReader reader = new JsonReader(reader("[null]"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expected a string but was NULL at line 1 column 2 path $[0]", expected.getMessage());
    }
  }

  public void testStrictNameValueSeparator() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\"=true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.nextBoolean();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 5 path $.a",
        expected.getMessage()
      );
    }

    reader = new JsonReader(reader("{\"a\"=>true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.nextBoolean();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 5 path $.a",
        expected.getMessage()
      );
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
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 5 path $.a",
        expected.getMessage()
      );
    }

    reader = new JsonReader(reader("{\"a\"=>true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 5 path $.a",
        expected.getMessage()
      );
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
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }

    reader = new JsonReader(reader("[# comment \n true]"));
    reader.beginArray();
    try {
      reader.nextBoolean();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }

    reader = new JsonReader(reader("[/* comment */ true]"));
    reader.beginArray();
    try {
      reader.nextBoolean();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
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
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }

    reader = new JsonReader(reader("[# comment \n true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }

    reader = new JsonReader(reader("[/* comment */ true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }
  }

  public void testStrictUnquotedNames() throws IOException {
    JsonReader reader = new JsonReader(reader("{a:true}"));
    reader.beginObject();
    try {
      reader.nextName();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $.",
        expected.getMessage()
      );
    }
  }

  public void testLenientUnquotedNames() throws IOException {
    JsonReader reader = new JsonReader(reader("{a:true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
  }

  public void testStrictUnquotedNamesWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("{a:true}"));
    reader.beginObject();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $.",
        expected.getMessage()
      );
    }
  }

  public void testStrictSingleQuotedNames() throws IOException {
    JsonReader reader = new JsonReader(reader("{'a':true}"));
    reader.beginObject();
    try {
      reader.nextName();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $.",
        expected.getMessage()
      );
    }
  }

  public void testLenientSingleQuotedNames() throws IOException {
    JsonReader reader = new JsonReader(reader("{'a':true}"));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
  }

  public void testStrictSingleQuotedNamesWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("{'a':true}"));
    reader.beginObject();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $.",
        expected.getMessage()
      );
    }
  }

  public void testStrictUnquotedStrings() throws IOException {
    JsonReader reader = new JsonReader(reader("[a]"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }
  }

  public void testStrictUnquotedStringsWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[a]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }
  }

  public void testLenientUnquotedStrings() throws IOException {
    JsonReader reader = new JsonReader(reader("[a]"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals("a", reader.nextString());
  }

  public void testStrictSingleQuotedStrings() throws IOException {
    JsonReader reader = new JsonReader(reader("['a']"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }
  }

  public void testLenientSingleQuotedStrings() throws IOException {
    JsonReader reader = new JsonReader(reader("['a']"));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals("a", reader.nextString());
  }

  public void testStrictSingleQuotedStringsWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("['a']"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }
  }

  public void testStrictSemicolonDelimitedArray() throws IOException {
    JsonReader reader = new JsonReader(reader("[true;true]"));
    reader.beginArray();
    reader.nextBoolean();
    try {
      reader.nextBoolean();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 6 path $[1]",
        expected.getMessage()
      );
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
    reader.skipValue();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 6 path $[1]",
        expected.getMessage()
      );
    }
  }

  public void testStrictSemicolonDelimitedNameValuePair() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":true;\"b\":true}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    reader.nextBoolean();
    try {
      reader.nextName();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 10 path $.a",
        expected.getMessage()
      );
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
    reader.skipValue();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 10 path $.null",
        expected.getMessage()
      );
    }
  }

  public void testStrictUnnecessaryArraySeparators() throws IOException {
    JsonReader reader = new JsonReader(reader("[true,,true]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    try {
      reader.nextNull();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 7 path $[1]",
        expected.getMessage()
      );
    }

    reader = new JsonReader(reader("[,true]"));
    reader.beginArray();
    try {
      reader.nextNull();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }

    reader = new JsonReader(reader("[true,]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    try {
      reader.nextNull();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 7 path $[1]",
        expected.getMessage()
      );
    }

    reader = new JsonReader(reader("[,]"));
    reader.beginArray();
    try {
      reader.nextNull();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
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
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 7 path $[1]",
        expected.getMessage()
      );
    }

    reader = new JsonReader(reader("[,true]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }

    reader = new JsonReader(reader("[true,]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 7 path $[1]",
        expected.getMessage()
      );
    }

    reader = new JsonReader(reader("[,]"));
    reader.beginArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }
  }

  public void testStrictMultipleTopLevelValues() throws IOException {
    JsonReader reader = new JsonReader(reader("[] []"));
    reader.beginArray();
    reader.endArray();
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 4 path $",
        expected.getMessage()
      );
    }
  }

  public void testLenientMultipleTopLevelValues() throws IOException {
    JsonReader reader = new JsonReader(reader("[] true {} a 'abc' /* test */ \"def\" # comment \n 123 null"));
    reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertEquals(true, reader.nextBoolean());
    reader.beginObject();
    reader.endObject();
    assertEquals("a", reader.nextString());
    assertEquals("abc", reader.nextString());
    assertEquals("def", reader.nextString());
    assertEquals(JsonToken.NUMBER, reader.peek());
    assertEquals(123, reader.nextInt());
    reader.nextNull();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testStrictMultipleTopLevelValuesWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("[] []"));
    reader.beginArray();
    reader.endArray();
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 4 path $",
        expected.getMessage()
      );
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
  }

  public void testTopLevelValueTypeWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader("true"));
    reader.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testStrictNonExecutePrefix() throws IOException {
    JsonReader reader = new JsonReader(reader(")]}'\n []"));
    try {
      reader.beginArray();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $",
        expected.getMessage()
      );
    }
  }

  public void testStrictNonExecutePrefixWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(reader(")]}'\n []"));
    try {
      reader.skipValue();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $",
        expected.getMessage()
      );
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
      assertEquals("Expected value at line 1 column 2 path $", expected.getMessage());
    }
  }

  /**
   * At most one non-execute prefix must be consumed.
   */
  public void testLenientDoubleNonExecutePrefix() throws IOException {
    JsonReader reader = new JsonReader(reader(")]}'\n)]}'\n 1"));
    reader.setLenient(true);
    // Consumes the parenthesis after the first non-execute prefix
    assertEquals(")", reader.nextString());
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals("Expected value at line 1 column 7 path $", expected.getMessage());
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
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
    }
  }

  public void testFailWithPosition() throws IOException {
    testFailWithPosition("Expected value at line 6 column 5 path $[1]",
        "[\n\n\n\n\n\"a\",}]");
  }

  public void testFailWithPositionGreaterThanBufferSize() throws IOException {
    String spaces = repeat(' ', 8192);
    testFailWithPosition("Expected value at line 6 column 5 path $[1]",
        "[\n\n" + spaces + "\n\n\n\"a\",}]");
  }

  public void testFailWithPositionOverSlashSlashEndOfLineComment() throws IOException {
    testFailWithPosition("Expected value at line 5 column 6 path $[1]",
        "\n// foo\n\n//bar\r\n[\"a\",}");
  }

  public void testFailWithPositionOverHashEndOfLineComment() throws IOException {
    testFailWithPosition("Expected value at line 5 column 6 path $[1]",
        "\n# foo\n\n#bar\r\n[\"a\",}");
  }

  public void testFailWithPositionOverCStyleComment() throws IOException {
    testFailWithPosition("Expected value at line 6 column 12 path $[1]",
        "\n\n/* foo\n*\n*\r\nbar */[\"a\",}");
  }

  public void testFailWithPositionOverQuotedString() throws IOException {
    testFailWithPosition("Expected value at line 5 column 3 path $[1]",
        "[\"foo\nbar\r\nbaz\n\",\n  }");
  }

  public void testFailWithPositionOverUnquotedString() throws IOException {
    testFailWithPosition("Expected value at line 5 column 2 path $[1]", "[\n\nabcd\n\n,}");
  }

  public void testFailWithEscapedNewlineCharacter() throws IOException {
    testFailWithPosition("Expected value at line 5 column 3 path $[1]", "[\n\n\"\\\n\n\",}");
  }

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
    } catch (MalformedJsonException expected) {
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
    } catch (MalformedJsonException expected) {
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
    } catch (MalformedJsonException expected) {
      assertEquals("Expected value at line 1 column 14 path $[1].a[2]", expected.getMessage());
    }
  }

  public void testStrictVeryLongNumber() throws IOException {
    JsonReader reader = new JsonReader(reader("[0." + repeat('9', 8192) + "]"));
    reader.beginArray();
    try {
      // Fails because JsonReader does not support arbitrarily long numbers in
      // strict mode
      reader.nextDouble();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(
        "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
        expected.getMessage()
      );
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
      assertEquals("Expected value at line 1 column 1 path $", expected.getMessage());
    }
  }

  public void testDocumentWithCommentEndingInSlash() throws IOException {
    JsonReader reader = new JsonReader(reader("/* foo *//"));
    reader.setLenient(true);
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals("Expected value at line 1 column 10 path $", expected.getMessage());
    }
  }

  public void testStringWithLeadingSlash() throws IOException {
    JsonReader reader = new JsonReader(reader("/x"));
    reader.setLenient(true);
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals("Expected value at line 1 column 1 path $", expected.getMessage());
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
      assertEquals("Unterminated object at line 1 column 15 path $.a", expected.getMessage());
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
      assertEquals("End of input at line 1 column 16386 path $[1]", expected.getMessage());
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

  public void testStrictExtraCommasInMaps() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"a\":\"b\",}"));
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals("b", reader.nextString());
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals("Expected name at line 1 column 10 path $.a", expected.getMessage());
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
    } catch (MalformedJsonException expected) {
      assertEquals("Expected name at line 1 column 10 path $.a", expected.getMessage());
    }
  }

  /**
   * When {@link JsonReader#peek()} throws an exception due to malformed JSON
   * it should not have advanced in the stream yet.
   */
  public void testThrowingPeekArray() throws IOException {
    JsonReader reader = new JsonReader(reader("[a?$,1]"));
    reader.beginArray();
    for (int i = 0; i < 10; i++) {
      try {
        reader.peek();
      } catch (MalformedJsonException expected) {
        assertEquals(
          "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]",
          expected.getMessage()
        );
      }
    }
  }

  /**
   * When {@link JsonReader#peek()} throws an exception due to malformed JSON
   * it should not have advanced in the stream yet.
   */
  public void testThrowingPeekObject() throws IOException {
    JsonReader reader = new JsonReader(reader("{\"test\"::}"));
    reader.beginObject();
    for (int i = 0; i < 10; i++) {
      try {
        reader.peek();
      } catch (MalformedJsonException expected) {
        assertEquals("Expected name at line 1 column 11 path $.a", expected.getMessage());
      }
    }
  }

  public void testThrowingPeekIncompleteBlockComment() throws IOException {
    JsonReader reader = new JsonReader(reader("[/*]"));
    reader.setLenient(true); // lenient to support block comments
    reader.beginArray();
    /*
     * Make sure that incomplete block comment (i.e. missing closing * /)
     * is not skipped after first unsuccessful peek.
     *
     * In previous Gson versions a subsequent peek would have skipped the
     * comment start (i.e. "/*") and therefore could have read "valid"
     * JSON afterwards.
     */
    for (int i = 0; i < 3; i++) {
      try {
        reader.peek();
      } catch (MalformedJsonException expected) {
        assertEquals("Unterminated comment at line 1 column 4 path $[0]", expected.getMessage());
      }
    }

    assertEquals("$[0]", reader.getPath());
  }

  public void testThrowingPeekEmptyDocument() throws IOException {
    JsonReader reader = new JsonReader(reader(":"));
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals("Expected value at line 1 column 1 path $", expected.getMessage());
    }
    /*
     * Make sure that JsonReader still considers document empty
     *
     * In previous Gson versions JsonReader would have marked document
     * as non-empty even though value parsing failed and would have
     * now thrown a non-lenient exception because it thought there were
     * multiple top-level values
     */
    try {
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals("Expected value at line 1 column 1 path $", expected.getMessage());
    }
  }

  public void testThrowingStringEscapeSequence() throws IOException {
    JsonReader reader = new JsonReader(reader("\"\\z\"")); // "\z" is not a valid escape sequence
    assertEquals(JsonToken.STRING, reader.peek());

    /*
     * Make sure that neither nextString() nor skipValue() already advanced
     * before throwing exception.
     *
     * In previous Gson versions they would have already consumed the '\' before
     * the exception was thrown so a subsequent read would have read a "valid"
     * string.
     */
    for (int i = 0; i < 3; i++) {
      try {
        reader.nextString();
        fail();
      } catch (MalformedJsonException expected) {
        assertEquals("Invalid escape sequence at line 1 column 2 path $", expected.getMessage());
      }
    }
    for (int i = 0; i < 3; i++) {
      try {
        reader.skipValue();
        fail();
      } catch (MalformedJsonException expected) {
        assertEquals("Invalid escape sequence at line 1 column 2 path $", expected.getMessage());
      }
    }
  }

  private String repeat(char c, int count) {
    char[] array = new char[count];
    Arrays.fill(array, c);
    return new String(array);
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
      assertEquals("Unterminated string at line 1 column 9 path $[0]", expected.getMessage());
    }
  }

  public void testStrictMalformedDocuments() throws IOException {
    // Incomplete or malformed comment should be considered not-a-name
    assertStrictDocument("{/", BEGIN_OBJECT, new MalformedJsonException("Expected name at line 1 column 2 path $."));
    assertStrictDocument("{/a", BEGIN_OBJECT, new MalformedJsonException("Expected name at line 1 column 2 path $."));
    assertStrictDocument("{//", BEGIN_OBJECT, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $."));
    assertStrictDocument("{/*", BEGIN_OBJECT, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $."));
    assertStrictDocument("{#", BEGIN_OBJECT, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $."));
    assertStrictDocument("{:", BEGIN_OBJECT, new MalformedJsonException("Expected name at line 1 column 2 path $."));
    assertStrictDocument("{,", BEGIN_OBJECT, new MalformedJsonException("Expected name at line 1 column 2 path $."));
    assertStrictDocument("{;", BEGIN_OBJECT, new MalformedJsonException("Expected name at line 1 column 2 path $."));
    assertStrictDocument("{a", BEGIN_OBJECT, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $."));
    // Incomplete or malformed comment should be considered not-a-value
    assertStrictDocument("{\"name\":/", BEGIN_OBJECT, NAME, new MalformedJsonException("Expected value at line 1 column 9 path $.name"));
    assertStrictDocument("{\"name\":/a", BEGIN_OBJECT, NAME, new MalformedJsonException("Expected value at line 1 column 9 path $.name"));
    assertStrictDocument("{\"name\"://", BEGIN_OBJECT, NAME, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 9 path $.name"));
    assertStrictDocument("{\"name\":/*", BEGIN_OBJECT, NAME, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 9 path $.name"));
    assertStrictDocument("{\"name\":#", BEGIN_OBJECT, NAME, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 9 path $.name"));
    assertStrictDocument("{\"name\"::", BEGIN_OBJECT, NAME, new MalformedJsonException("Expected value at line 1 column 9 path $.name"));
    assertStrictDocument("{\"name\":,", BEGIN_OBJECT, NAME, new MalformedJsonException("Expected value at line 1 column 9 path $.name"));
    assertStrictDocument("{\"name\":;", BEGIN_OBJECT, NAME, new MalformedJsonException("Expected value at line 1 column 9 path $.name"));
    assertStrictDocument("{\"name\":a", BEGIN_OBJECT, NAME, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 9 path $.name"));

    // Incomplete or malformed comment should be considered not-a-value
    assertStrictDocument("[/", BEGIN_ARRAY, new MalformedJsonException("Expected value at line 1 column 2 path $[0]"));
    assertStrictDocument("[/a", BEGIN_ARRAY, new MalformedJsonException("Expected value at line 1 column 2 path $[0]"));
    assertStrictDocument("[//", BEGIN_ARRAY, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]"));
    assertStrictDocument("[/*", BEGIN_ARRAY, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]"));
    assertStrictDocument("[#", BEGIN_ARRAY, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]"));
    assertStrictDocument("[:", BEGIN_ARRAY, new MalformedJsonException("Expected value at line 1 column 2 path $[0]"));
    assertStrictDocument("[;", BEGIN_ARRAY, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]"));
    assertStrictDocument("[a", BEGIN_ARRAY, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $[0]"));

    // Incomplete or malformed comment should be considered not-a-value
    assertStrictDocument("/", new MalformedJsonException("Expected value at line 1 column 1 path $"));
    assertStrictDocument("/a", new MalformedJsonException("Expected value at line 1 column 1 path $"));
    assertStrictDocument("//", new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $"));
    assertStrictDocument("/*", new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $"));
    assertStrictDocument("#", new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $"));
    assertStrictDocument(":", new MalformedJsonException("Expected value at line 1 column 1 path $"));
    assertStrictDocument(";", new MalformedJsonException("Expected value at line 1 column 1 path $"));
    assertStrictDocument("a", new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $"));
    assertStrictDocument(" ", new EOFException("End of input at line 1 column 2 path $"));

    // Incomplete or malformed comment should be considered not-a-value
    assertStrictDocument("false/", BOOLEAN, new MalformedJsonException("Unexpected character at line 1 column 6 path $"));
    assertStrictDocument("false/a", BOOLEAN, new MalformedJsonException("Unexpected character at line 1 column 6 path $"));
    assertStrictDocument("false//", BOOLEAN, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 6 path $"));
    assertStrictDocument("false/*", BOOLEAN, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 6 path $"));
    assertStrictDocument("false#", BOOLEAN, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 6 path $"));
    assertStrictDocument("false:", BOOLEAN, new MalformedJsonException("Unexpected character at line 1 column 6 path $"));
    assertStrictDocument("false,", BOOLEAN, new MalformedJsonException("Unexpected character at line 1 column 6 path $"));
    assertStrictDocument("false;", BOOLEAN, new MalformedJsonException("Unexpected character at line 1 column 6 path $"));
    assertStrictDocument("falsea", new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $"));
    assertStrictDocument("false ", BOOLEAN, END_DOCUMENT); // well-formed

    // Incomplete or malformed comment should be considered not-a-value
    assertStrictDocument("123/", NUMBER, new MalformedJsonException("Unexpected character at line 1 column 4 path $"));
    assertStrictDocument("123/a", NUMBER, new MalformedJsonException("Unexpected character at line 1 column 4 path $"));
    assertStrictDocument("123/*", NUMBER, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 4 path $"));
    assertStrictDocument("123//", NUMBER, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 4 path $"));
    assertStrictDocument("123#", NUMBER, new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 4 path $"));
    assertStrictDocument("123:", NUMBER, new MalformedJsonException("Unexpected character at line 1 column 4 path $"));
    assertStrictDocument("123,", NUMBER, new MalformedJsonException("Unexpected character at line 1 column 4 path $"));
    assertStrictDocument("123;", NUMBER, new MalformedJsonException("Unexpected character at line 1 column 4 path $"));
    assertStrictDocument("123a", new MalformedJsonException("Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $"));
    assertStrictDocument("123 ", NUMBER, END_DOCUMENT); // well-formed
  }

  public void testLenientMalformedDocuments() throws IOException {
    assertLenientDocument("{", BEGIN_OBJECT, new EOFException("End of input at line 1 column 2 path $."));
    assertLenientDocument("{/", BEGIN_OBJECT, new MalformedJsonException("Expected name at line 1 column 2 path $."));
    assertLenientDocument("{/a", BEGIN_OBJECT, new MalformedJsonException("Expected name at line 1 column 2 path $."));
    assertLenientDocument("{]", BEGIN_OBJECT, new MalformedJsonException("Expected name at line 1 column 2 path $."));
    assertLenientDocument("{,", BEGIN_OBJECT, new MalformedJsonException("Expected name at line 1 column 2 path $."));
    assertLenientDocument("{{", BEGIN_OBJECT, new MalformedJsonException("Expected name at line 1 column 2 path $."));
    assertLenientDocument("{[", BEGIN_OBJECT, new MalformedJsonException("Expected name at line 1 column 2 path $."));
    assertLenientDocument("{:", BEGIN_OBJECT, new MalformedJsonException("Expected name at line 1 column 2 path $."));
    assertLenientDocument("{\"name\"", BEGIN_OBJECT, NAME, new EOFException("End of input at line 1 column 8 path $.name"));
    assertLenientDocument("{'name'", BEGIN_OBJECT, NAME, new EOFException("End of input at line 1 column 8 path $.name"));
    assertLenientDocument("{name", BEGIN_OBJECT, NAME, new EOFException("End of input at line 1 column 6 path $.name"));
    assertLenientDocument("{\"name\",", BEGIN_OBJECT, NAME, new MalformedJsonException("Expected ':' at line 1 column 8 path $.name"));
    assertLenientDocument("{\"name\";", BEGIN_OBJECT, NAME, new MalformedJsonException("Expected ':' at line 1 column 8 path $.name"));
    assertLenientDocument("{'name',", BEGIN_OBJECT, NAME, new MalformedJsonException("Expected ':' at line 1 column 8 path $.name"));
    assertLenientDocument("{'name';", BEGIN_OBJECT, NAME, new MalformedJsonException("Expected ':' at line 1 column 8 path $.name"));
    assertLenientDocument("{\"name\":}", BEGIN_OBJECT, NAME, new MalformedJsonException("Expected value at line 1 column 9 path $.name"));
    assertLenientDocument("{\"name\"::", BEGIN_OBJECT, NAME, new MalformedJsonException("Expected value at line 1 column 9 path $.name"));
    assertLenientDocument("{\"name\":,", BEGIN_OBJECT, NAME, new MalformedJsonException("Expected value at line 1 column 9 path $.name"));
    assertLenientDocument("{\"name\"=}", BEGIN_OBJECT, NAME, new MalformedJsonException("Expected value at line 1 column 9 path $.name"));
    assertLenientDocument("{\"name\"=>}", BEGIN_OBJECT, NAME, new MalformedJsonException("Expected value at line 1 column 10 path $.name"));
    assertLenientDocument("{\"name\":\"string\"", BEGIN_OBJECT, NAME, STRING, new EOFException("End of input at line 1 column 17 path $.name"));
    assertLenientDocument("{\"name\":'string'", BEGIN_OBJECT, NAME, STRING, new EOFException("End of input at line 1 column 17 path $.name"));
    assertLenientDocument("{\"name\":123", BEGIN_OBJECT, NAME, NUMBER, new EOFException("End of input at line 1 column 12 path $.name"));
    assertLenientDocument("{\"name\":false", BEGIN_OBJECT, NAME, BOOLEAN, new EOFException("End of input at line 1 column 14 path $.name"));
    assertLenientDocument("{\"name\"=>\"string\":", BEGIN_OBJECT, NAME, STRING, new MalformedJsonException("Unterminated object at line 1 column 18 path $.name"));
    assertLenientDocument("{\"name\"=>\"string\"=", BEGIN_OBJECT, NAME, STRING, new MalformedJsonException("Unterminated object at line 1 column 18 path $.name"));
    assertLenientDocument("{\"name\"=>\"string\"=>", BEGIN_OBJECT, NAME, STRING, new MalformedJsonException("Unterminated object at line 1 column 18 path $.name"));
    assertLenientDocument("{\"name\":\"string\",", BEGIN_OBJECT, NAME, STRING, new EOFException("End of input at line 1 column 18 path $.name"));
    assertLenientDocument("{\"name\":'string',", BEGIN_OBJECT, NAME, STRING, new EOFException("End of input at line 1 column 18 path $.name"));
    assertLenientDocument("{\"name\"=>\"string\",", BEGIN_OBJECT, NAME, STRING, new EOFException("End of input at line 1 column 19 path $.name"));
    assertLenientDocument("{\"name\":123,", BEGIN_OBJECT, NAME, NUMBER, new EOFException("End of input at line 1 column 13 path $.name"));
    assertLenientDocument("{\"name\":false,,", BEGIN_OBJECT, NAME, BOOLEAN, new MalformedJsonException("Expected name at line 1 column 15 path $.name"));
    assertLenientDocument("{\"name\"=>\"string\",\"name\"", BEGIN_OBJECT, NAME, STRING, NAME, new EOFException("End of input at line 1 column 25 path $.name"));

    assertLenientDocument("[", BEGIN_ARRAY, new EOFException("End of input at line 1 column 2 path $[0]"));
    assertLenientDocument("[}", BEGIN_ARRAY, new MalformedJsonException("Expected value at line 1 column 2 path $[0]"));
    assertLenientDocument("[,]", BEGIN_ARRAY, NULL, NULL, END_ARRAY); // well-formed
    assertLenientDocument("[string", BEGIN_ARRAY, STRING, new EOFException("End of input at line 1 column 8 path $[1]"));
    assertLenientDocument("[\"string\"", BEGIN_ARRAY, STRING, new EOFException("End of input at line 1 column 10 path $[1]"));
    assertLenientDocument("['string'", BEGIN_ARRAY, STRING, new EOFException("End of input at line 1 column 10 path $[1]"));
    assertLenientDocument("[123", BEGIN_ARRAY, NUMBER, new EOFException("End of input at line 1 column 5 path $[1]"));
    assertLenientDocument("[123,", BEGIN_ARRAY, NUMBER, new EOFException("End of input at line 1 column 6 path $[1]"));
    assertLenientDocument("[123[", BEGIN_ARRAY, NUMBER, new MalformedJsonException("Unterminated array at line 1 column 5 path $[1]"));
    assertLenientDocument("[\"string\":false", BEGIN_ARRAY, STRING, new MalformedJsonException("Unterminated array at line 1 column 10 path $[1]"));
    assertLenientDocument("[123,}", BEGIN_ARRAY, NUMBER, new MalformedJsonException("Expected value at line 1 column 6 path $[1]"));
  }

  private static void assertStrictDocument(String document, Object... expectations) throws IOException {
    assertDocument(false, document, expectations);
  }

  private static void assertLenientDocument(String document, Object... expectations) throws IOException {
    assertDocument(true, document, expectations);
  }

  private static void assertDocument(boolean lenient, String document, Object... expectations) throws IOException {
    JsonReader reader = new JsonReader(reader(document));
    reader.setLenient(lenient);
    for (Object expectation : expectations) {
      if (expectation == BEGIN_OBJECT) {
        reader.beginObject();
      } else if (expectation == BEGIN_ARRAY) {
        reader.beginArray();
      } else if (expectation == END_OBJECT) {
        reader.endObject();
      } else if (expectation == END_ARRAY) {
        reader.endArray();
      } else if (expectation == END_DOCUMENT) {
        assertEquals(expectation, reader.peek());
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
      } else if (expectation instanceof Exception) {
        try {
          reader.peek();
          fail();
        } catch (Exception thrown) {
          assertEquals(expectation.getClass(), thrown.getClass());
          assertEquals(((Exception) expectation).getMessage(), thrown.getMessage());
        }
      } else {
        throw new AssertionError();
      }
    }
  }

  private static Reader reader(final String s) {
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
