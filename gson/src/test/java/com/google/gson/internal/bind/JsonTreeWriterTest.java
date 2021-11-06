/*
 * Copyright (C) 2011 Google Inc.
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

package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.CharBuffer;
import junit.framework.TestCase;

@SuppressWarnings("resource")
public final class JsonTreeWriterTest extends TestCase {
  public void testArray() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.value(1);
    writer.value(2);
    writer.value(3);
    writer.endArray();
    assertEquals("[1,2,3]", writer.get().toString());
  }

  public void testNestedArray() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.beginArray();
    writer.endArray();
    writer.beginArray();
    writer.beginArray();
    writer.endArray();
    writer.endArray();
    writer.endArray();
    assertEquals("[[],[[]]]", writer.get().toString());
  }

  public void testObject() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("A").value(1);
    writer.name("B").value(2);
    writer.endObject();
    assertEquals("{\"A\":1,\"B\":2}", writer.get().toString());
  }

  public void testNestedObject() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("A");
    writer.beginObject();
    writer.name("B");
    writer.beginObject();
    writer.endObject();
    writer.endObject();
    writer.name("C");
    writer.beginObject();
    writer.endObject();
    writer.endObject();
    assertEquals("{\"A\":{\"B\":{}},\"C\":{}}", writer.get().toString());
  }

  public void testWriteAfterClose() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.beginArray();
    writer.value("A");
    writer.endArray();
    writer.close();
    try {
      writer.beginArray();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  public void testPrematureClose() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.beginArray();
    try {
      writer.close();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testSerializeNullsFalse() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setSerializeNulls(false);
    writer.beginObject();
    writer.name("A");
    writer.nullValue();
    writer.endObject();
    assertEquals("{}", writer.get().toString());
  }

  public void testSerializeNullsTrue() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setSerializeNulls(true);
    writer.beginObject();
    writer.name("A");
    writer.nullValue();
    writer.endObject();
    assertEquals("{\"A\":null}", writer.get().toString());
  }

  public void testEmptyWriter() {
    JsonTreeWriter writer = new JsonTreeWriter();
    assertEquals(JsonNull.INSTANCE, writer.get());
  }

  public void testBeginEndArray() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    assertSame(writer, writer.beginArray());
    assertSame(writer, writer.endArray());

    assertEquals(new JsonArray(), writer.get());
  }

  public void testBeginEndObject() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    assertSame(writer, writer.beginObject());
    assertSame(writer, writer.name("a"));
    assertSame(writer, writer.value(1));
    assertSame(writer, writer.endObject());

    JsonObject expected = new JsonObject();
    expected.addProperty("a", 1);
    assertEquals(expected, writer.get());
  }

  public void testValueString() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    String n = "as";
    assertSame(writer, writer.value(n));

    assertEquals(new JsonPrimitive(n), writer.get());
  }

  public void testValueCharSequence() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    String n = "as";
    assertSame(writer, writer.value(CharBuffer.wrap(n.toCharArray())));

    assertEquals(new JsonPrimitive(n), writer.get());
  }

  public void testBooleanValue() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    boolean bool = true;
    assertSame(writer, writer.value(bool));

    assertEquals(new JsonPrimitive(bool), writer.get());
  }

  public void testBooleanWrappedValue() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    Boolean bool = true;
    assertSame(writer, writer.value(bool));

    assertEquals(new JsonPrimitive(bool), writer.get());
  }

  public void testDoubleValue() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    double value = 123.45;
    assertSame(writer, writer.value(value));

    assertEquals(new JsonPrimitive(value), writer.get());
  }

  public void testLongValue() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    long value = 1234L;
    assertSame(writer, writer.value(value));

    assertEquals(new JsonPrimitive(value), writer.get());
  }

  public void testNumberValue() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    BigInteger value = new BigInteger("1234");
    assertSame(writer, writer.value(value));

    assertEquals(new JsonPrimitive(value), writer.get());
  }

  public void testNullValue() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    assertSame(writer, writer.nullValue());

    assertEquals(JsonNull.INSTANCE, writer.get());
  }

  public void testLenientNansAndInfinities() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.beginArray();
    writer.value(Double.NaN);
    writer.value(Double.NEGATIVE_INFINITY);
    writer.value(Double.POSITIVE_INFINITY);
    writer.endArray();
    assertEquals("[NaN,-Infinity,Infinity]", writer.get().toString());
  }

  public void testStrictNansAndInfinities() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(false);
    writer.beginArray();
    try {
      writer.value(Double.NaN);
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      writer.value(Double.NEGATIVE_INFINITY);
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      writer.value(Double.POSITIVE_INFINITY);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testStrictBoxedNansAndInfinities() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(false);
    writer.beginArray();
    try {
      writer.value(Double.valueOf(Double.NaN));
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      writer.value(Double.valueOf(Double.NEGATIVE_INFINITY));
      fail();
    } catch (IllegalArgumentException expected) {
    }
    try {
      writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }
}
