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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.FormattingStyle;
import com.google.gson.internal.LazilyParsedNumber;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;

@SuppressWarnings("resource")
public final class JsonWriterTest {

  @Test
  public void testTopLevelValueTypes() throws IOException {
    StringWriter string1 = new StringWriter();
    JsonWriter writer1 = new JsonWriter(string1);
    writer1.value(true);
    writer1.close();
    assertThat(string1.toString()).isEqualTo("true");

    StringWriter string2 = new StringWriter();
    JsonWriter writer2 = new JsonWriter(string2);
    writer2.nullValue();
    writer2.close();
    assertThat(string2.toString()).isEqualTo("null");

    StringWriter string3 = new StringWriter();
    JsonWriter writer3 = new JsonWriter(string3);
    writer3.value(123);
    writer3.close();
    assertThat(string3.toString()).isEqualTo("123");

    StringWriter string4 = new StringWriter();
    JsonWriter writer4 = new JsonWriter(string4);
    writer4.value(123.4);
    writer4.close();
    assertThat(string4.toString()).isEqualTo("123.4");

    StringWriter string5 = new StringWriter();
    JsonWriter writert = new JsonWriter(string5);
    writert.value("a");
    writert.close();
    assertThat(string5.toString()).isEqualTo("\"a\"");
  }

  @Test
  public void testInvalidTopLevelTypes() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.name("hello");
    try {
      jsonWriter.value("world");
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testTwoNames() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.name("a");
    try {
      jsonWriter.name("a");
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testNameWithoutValue() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.name("a");
    try {
      jsonWriter.endObject();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testValueWithoutName() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    try {
      jsonWriter.value(true);
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testMultipleTopLevelValues() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray().endArray();
    try {
      jsonWriter.beginArray();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testBadNestingObject() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.beginObject();
    try {
      jsonWriter.endArray();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testBadNestingArray() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.beginArray();
    try {
      jsonWriter.endObject();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testNullName() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    try {
      jsonWriter.name(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testNullStringValue() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.name("a");
    jsonWriter.value((String) null);
    jsonWriter.endObject();
    assertThat(stringWriter.toString()).isEqualTo("{\"a\":null}");
  }

  @Test
  public void testJsonValue() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.name("a");
    jsonWriter.jsonValue("{\"b\":true}");
    jsonWriter.name("c");
    jsonWriter.value(1);
    jsonWriter.endObject();
    assertThat(stringWriter.toString()).isEqualTo("{\"a\":{\"b\":true},\"c\":1}");
  }

  @Test
  public void testNonFiniteFloats() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    try {
      jsonWriter.value(Float.NaN);
      fail();
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was NaN");
    }
    try {
      jsonWriter.value(Float.NEGATIVE_INFINITY);
      fail();
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was -Infinity");
    }
    try {
      jsonWriter.value(Float.POSITIVE_INFINITY);
      fail();
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was Infinity");
    }
  }

  @Test
  public void testNonFiniteDoubles() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    try {
      jsonWriter.value(Double.NaN);
      fail();
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was NaN");
    }
    try {
      jsonWriter.value(Double.NEGATIVE_INFINITY);
      fail();
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was -Infinity");
    }
    try {
      jsonWriter.value(Double.POSITIVE_INFINITY);
      fail();
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was Infinity");
    }
  }

  @Test
  public void testNonFiniteNumbers() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    try {
      jsonWriter.value(Double.valueOf(Double.NaN));
      fail();
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was NaN");
    }
    try {
      jsonWriter.value(Double.valueOf(Double.NEGATIVE_INFINITY));
      fail();
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was -Infinity");
    }
    try {
      jsonWriter.value(Double.valueOf(Double.POSITIVE_INFINITY));
      fail();
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was Infinity");
    }
    try {
      jsonWriter.value(new LazilyParsedNumber("Infinity"));
      fail();
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Numeric values must be finite, but was Infinity");
    }
  }

  @Test
  public void testNonFiniteFloatsWhenLenient() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setLenient(true);
    jsonWriter.beginArray();
    jsonWriter.value(Float.NaN);
    jsonWriter.value(Float.NEGATIVE_INFINITY);
    jsonWriter.value(Float.POSITIVE_INFINITY);
    jsonWriter.endArray();
    assertThat(stringWriter.toString()).isEqualTo("[NaN,-Infinity,Infinity]");
  }

  @Test
  public void testNonFiniteDoublesWhenLenient() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setLenient(true);
    jsonWriter.beginArray();
    jsonWriter.value(Double.NaN);
    jsonWriter.value(Double.NEGATIVE_INFINITY);
    jsonWriter.value(Double.POSITIVE_INFINITY);
    jsonWriter.endArray();
    assertThat(stringWriter.toString()).isEqualTo("[NaN,-Infinity,Infinity]");
  }

  @Test
  public void testNonFiniteNumbersWhenLenient() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setLenient(true);
    jsonWriter.beginArray();
    jsonWriter.value(Double.valueOf(Double.NaN));
    jsonWriter.value(Double.valueOf(Double.NEGATIVE_INFINITY));
    jsonWriter.value(Double.valueOf(Double.POSITIVE_INFINITY));
    jsonWriter.value(new LazilyParsedNumber("Infinity"));
    jsonWriter.endArray();
    assertThat(stringWriter.toString()).isEqualTo("[NaN,-Infinity,Infinity,Infinity]");
  }

  @Test
  public void testFloats() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value(-0.0f);
    jsonWriter.value(1.0f);
    jsonWriter.value(Float.MAX_VALUE);
    jsonWriter.value(Float.MIN_VALUE);
    jsonWriter.value(0.0f);
    jsonWriter.value(-0.5f);
    jsonWriter.value(2.2250739E-38f);
    jsonWriter.value(3.723379f);
    jsonWriter.value((float) Math.PI);
    jsonWriter.value((float) Math.E);
    jsonWriter.endArray();
    jsonWriter.close();
    assertThat(stringWriter.toString()).isEqualTo("[-0.0,"
        + "1.0,"
        + "3.4028235E38,"
        + "1.4E-45,"
        + "0.0,"
        + "-0.5,"
        + "2.2250739E-38,"
        + "3.723379,"
        + "3.1415927,"
        + "2.7182817]");
  }

  @Test
  public void testDoubles() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value(-0.0);
    jsonWriter.value(1.0);
    jsonWriter.value(Double.MAX_VALUE);
    jsonWriter.value(Double.MIN_VALUE);
    jsonWriter.value(0.0);
    jsonWriter.value(-0.5);
    jsonWriter.value(2.2250738585072014E-308);
    jsonWriter.value(Math.PI);
    jsonWriter.value(Math.E);
    jsonWriter.endArray();
    jsonWriter.close();
    assertThat(stringWriter.toString()).isEqualTo("[-0.0,"
        + "1.0,"
        + "1.7976931348623157E308,"
        + "4.9E-324,"
        + "0.0,"
        + "-0.5,"
        + "2.2250738585072014E-308,"
        + "3.141592653589793,"
        + "2.718281828459045]");
  }

  @Test
  public void testLongs() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value(0);
    jsonWriter.value(1);
    jsonWriter.value(-1);
    jsonWriter.value(Long.MIN_VALUE);
    jsonWriter.value(Long.MAX_VALUE);
    jsonWriter.endArray();
    jsonWriter.close();
    assertThat(stringWriter.toString()).isEqualTo("[0,"
        + "1,"
        + "-1,"
        + "-9223372036854775808,"
        + "9223372036854775807]");
  }

  @Test
  public void testNumbers() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value(new BigInteger("0"));
    jsonWriter.value(new BigInteger("9223372036854775808"));
    jsonWriter.value(new BigInteger("-9223372036854775809"));
    jsonWriter.value(new BigDecimal("3.141592653589793238462643383"));
    jsonWriter.endArray();
    jsonWriter.close();
    assertThat(stringWriter.toString()).isEqualTo("[0,"
        + "9223372036854775808,"
        + "-9223372036854775809,"
        + "3.141592653589793238462643383]");
  }

  /**
   * Tests writing {@code Number} instances which are not one of the standard JDK ones.
   */
  @Test
  public void testNumbersCustomClass() throws IOException {
    String[] validNumbers = {
        "-0.0",
        "1.0",
        "1.7976931348623157E308",
        "4.9E-324",
        "0.0",
        "0.00",
        "-0.5",
        "2.2250738585072014E-308",
        "3.141592653589793",
        "2.718281828459045",
        "0",
        "0.01",
        "0e0",
        "1e+0",
        "1e-0",
        "1e0000", // leading 0 is allowed for exponent
        "1e00001",
        "1e+1",
    };

    for (String validNumber : validNumbers) {
      StringWriter stringWriter = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(stringWriter);

      jsonWriter.value(new LazilyParsedNumber(validNumber));
      jsonWriter.close();

      assertThat(stringWriter.toString()).isEqualTo(validNumber);
    }
  }

  @Test
  public void testMalformedNumbers() throws IOException {
    String[] malformedNumbers = {
        "some text",
        "",
        ".",
        "00",
        "01",
        "-00",
        "-",
        "--1",
        "+1", // plus sign is not allowed for integer part
        "+",
        "1,0",
        "1,000",
        "0.", // decimal digit is required
        ".1", // integer part is required
        "e1",
        ".e1",
        ".1e1",
        "1e-",
        "1e+",
        "1e--1",
        "1e+-1",
        "1e1e1",
        "1+e1",
        "1e1.0",
    };

    for (String malformedNumber : malformedNumbers) {
      JsonWriter jsonWriter = new JsonWriter(new StringWriter());
      try {
        jsonWriter.value(new LazilyParsedNumber(malformedNumber));
        fail("Should have failed writing malformed number: " + malformedNumber);
      } catch (IllegalArgumentException e) {
        assertThat(e.getMessage()).isEqualTo("String created by class com.google.gson.internal.LazilyParsedNumber is not a valid JSON number: " + malformedNumber);
      }
    }
  }

  @Test
  public void testBooleans() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value(true);
    jsonWriter.value(false);
    jsonWriter.endArray();
    assertThat(stringWriter.toString()).isEqualTo("[true,false]");
  }

  @Test
  public void testBoxedBooleans() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value((Boolean) true);
    jsonWriter.value((Boolean) false);
    jsonWriter.value((Boolean) null);
    jsonWriter.endArray();
    assertThat(stringWriter.toString()).isEqualTo("[true,false,null]");
  }

  @Test
  public void testNulls() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.nullValue();
    jsonWriter.endArray();
    assertThat(stringWriter.toString()).isEqualTo("[null]");
  }

  @Test
  public void testStrings() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value("a");
    jsonWriter.value("a\"");
    jsonWriter.value("\"");
    jsonWriter.value(":");
    jsonWriter.value(",");
    jsonWriter.value("\b");
    jsonWriter.value("\f");
    jsonWriter.value("\n");
    jsonWriter.value("\r");
    jsonWriter.value("\t");
    jsonWriter.value(" ");
    jsonWriter.value("\\");
    jsonWriter.value("{");
    jsonWriter.value("}");
    jsonWriter.value("[");
    jsonWriter.value("]");
    jsonWriter.value("\0");
    jsonWriter.value("\u0019");
    jsonWriter.endArray();
    assertThat(stringWriter.toString()).isEqualTo("[\"a\","
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
        + "\"\\u0019\"]");
  }

  @Test
  public void testUnicodeLineBreaksEscaped() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.value("\u2028 \u2029");
    jsonWriter.endArray();
    assertThat(stringWriter.toString()).isEqualTo("[\"\\u2028 \\u2029\"]");
  }

  @Test
  public void testEmptyArray() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.endArray();
    assertThat(stringWriter.toString()).isEqualTo("[]");
  }

  @Test
  public void testEmptyObject() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.endObject();
    assertThat(stringWriter.toString()).isEqualTo("{}");
  }

  @Test
  public void testObjectsInArrays() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.beginObject();
    jsonWriter.name("a").value(5);
    jsonWriter.name("b").value(false);
    jsonWriter.endObject();
    jsonWriter.beginObject();
    jsonWriter.name("c").value(6);
    jsonWriter.name("d").value(true);
    jsonWriter.endObject();
    jsonWriter.endArray();
    assertThat(stringWriter.toString()).isEqualTo("[{\"a\":5,\"b\":false},"
        + "{\"c\":6,\"d\":true}]");
  }

  @Test
  public void testArraysInObjects() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.name("a");
    jsonWriter.beginArray();
    jsonWriter.value(5);
    jsonWriter.value(false);
    jsonWriter.endArray();
    jsonWriter.name("b");
    jsonWriter.beginArray();
    jsonWriter.value(6);
    jsonWriter.value(true);
    jsonWriter.endArray();
    jsonWriter.endObject();
    assertThat(stringWriter.toString()).isEqualTo("{\"a\":[5,false],"
        + "\"b\":[6,true]}");
  }

  @Test
  public void testDeepNestingArrays() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    for (int i = 0; i < 20; i++) {
      jsonWriter.beginArray();
    }
    for (int i = 0; i < 20; i++) {
      jsonWriter.endArray();
    }
    assertThat(stringWriter.toString()).isEqualTo("[[[[[[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]]]]]]");
  }

  @Test
  public void testDeepNestingObjects() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    for (int i = 0; i < 20; i++) {
      jsonWriter.name("a");
      jsonWriter.beginObject();
    }
    for (int i = 0; i < 20; i++) {
      jsonWriter.endObject();
    }
    jsonWriter.endObject();
    assertThat(stringWriter.toString()).isEqualTo("{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":"
        + "{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{"
        + "}}}}}}}}}}}}}}}}}}}}}");
  }

  @Test
  public void testRepeatedName() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.name("a").value(true);
    jsonWriter.name("a").value(false);
    jsonWriter.endObject();
    // JsonWriter doesn't attempt to detect duplicate names
    assertThat(stringWriter.toString()).isEqualTo("{\"a\":true,\"a\":false}");
  }

  @Test
  public void testPrettyPrintObject() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setIndent("   ");

    jsonWriter.beginObject();
    jsonWriter.name("a").value(true);
    jsonWriter.name("b").value(false);
    jsonWriter.name("c").value(5.0);
    jsonWriter.name("e").nullValue();
    jsonWriter.name("f").beginArray();
    jsonWriter.value(6.0);
    jsonWriter.value(7.0);
    jsonWriter.endArray();
    jsonWriter.name("g").beginObject();
    jsonWriter.name("h").value(8.0);
    jsonWriter.name("i").value(9.0);
    jsonWriter.endObject();
    jsonWriter.endObject();

    String expected = "{\n"
        + "   \"a\": true,\n"
        + "   \"b\": false,\n"
        + "   \"c\": 5.0,\n"
        + "   \"e\": null,\n"
        + "   \"f\": [\n"
        + "      6.0,\n"
        + "      7.0\n"
        + "   ],\n"
        + "   \"g\": {\n"
        + "      \"h\": 8.0,\n"
        + "      \"i\": 9.0\n"
        + "   }\n"
        + "}";
    assertThat(stringWriter.toString()).isEqualTo(expected);
  }

  @Test
  public void testPrettyPrintArray() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setIndent("   ");

    jsonWriter.beginArray();
    jsonWriter.value(true);
    jsonWriter.value(false);
    jsonWriter.value(5.0);
    jsonWriter.nullValue();
    jsonWriter.beginObject();
    jsonWriter.name("a").value(6.0);
    jsonWriter.name("b").value(7.0);
    jsonWriter.endObject();
    jsonWriter.beginArray();
    jsonWriter.value(8.0);
    jsonWriter.value(9.0);
    jsonWriter.endArray();
    jsonWriter.endArray();

    String expected = "[\n"
        + "   true,\n"
        + "   false,\n"
        + "   5.0,\n"
        + "   null,\n"
        + "   {\n"
        + "      \"a\": 6.0,\n"
        + "      \"b\": 7.0\n"
        + "   },\n"
        + "   [\n"
        + "      8.0,\n"
        + "      9.0\n"
        + "   ]\n"
        + "]";
    assertThat(stringWriter.toString()).isEqualTo(expected);
  }

  @Test
  public void testLenientWriterPermitsMultipleTopLevelValues() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.setLenient(true);
    writer.beginArray();
    writer.endArray();
    writer.beginArray();
    writer.endArray();
    writer.close();
    assertThat(stringWriter.toString()).isEqualTo("[][]");
  }

  @Test
  public void testStrictWriterDoesNotPermitMultipleTopLevelValues() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.endArray();
    try {
      writer.beginArray();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testClosedWriterThrowsOnStructure() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.endArray();
    writer.close();
    try {
      writer.beginArray();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      writer.endArray();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      writer.beginObject();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      writer.endObject();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testClosedWriterThrowsOnName() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.endArray();
    writer.close();
    try {
      writer.name("a");
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testClosedWriterThrowsOnValue() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.endArray();
    writer.close();
    try {
      writer.value("a");
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testClosedWriterThrowsOnFlush() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.endArray();
    writer.close();
    try {
      writer.flush();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testWriterCloseIsIdempotent() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(stringWriter);
    writer.beginArray();
    writer.endArray();
    writer.close();
    writer.close();
  }

  @Test
  public void testSetGetFormattingStyle() throws IOException {
    String lineSeparator = "\r\n";

    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setFormattingStyle(FormattingStyle.DEFAULT.withIndent(" \t ").withNewline(lineSeparator));

    jsonWriter.beginArray();
    jsonWriter.value(true);
    jsonWriter.value("text");
    jsonWriter.value(5.0);
    jsonWriter.nullValue();
    jsonWriter.endArray();

    String expected = "[\r\n"
        + " \t true,\r\n"
        + " \t \"text\",\r\n"
        + " \t 5.0,\r\n"
        + " \t null\r\n"
        + "]";
    assertThat(stringWriter.toString()).isEqualTo(expected);

    assertThat(jsonWriter.getFormattingStyle().getNewline()).isEqualTo(lineSeparator);
  }
}
