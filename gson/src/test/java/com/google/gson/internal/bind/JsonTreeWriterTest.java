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

import java.io.IOException;
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
      assertEquals("Writer is closed", expected.getMessage());
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
      assertEquals("Incomplete document", expected.getMessage());
    }
    // Should prevent further interaction nonetheless
    try {
      writer.endArray();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
  }

  public void testClosedWriterDuplicateName() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("test");
    try {
      writer.close();
      fail();
    } catch (IOException expected) {
      assertEquals("Incomplete document", expected.getMessage());
    }
    // JsonTreeWriter being closed should have higher precedence than duplicate name
    try {
      writer.name("test");
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
  }

  public void testClosedWriterDontSerializeNulls() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setSerializeNulls(false);
    writer.beginObject();
    writer.name("test");
    try {
      writer.close();
      fail();
    } catch (IOException expected) {
      assertEquals("Incomplete document", expected.getMessage());
    }
    // JsonTreeWriter being closed should be checked, even if null is not serialized
    try {
      writer.nullValue();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
  }

  public void testCloseEmptyWriter() {
    JsonTreeWriter writer = new JsonTreeWriter();
    try {
      writer.close();
      fail();
    } catch (IOException expected) {
      assertEquals("Incomplete document", expected.getMessage());
    }
  }

  public void testGetAfterClose() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value("test");
    writer.close();
    assertEquals("\"test\"", writer.get().toString());
  }

  public void testClosedWriterThrowsOnStructure() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.endArray();
    writer.close();
    try {
      writer.beginArray();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
    try {
      writer.endArray();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
    try {
      writer.beginObject();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
    try {
      writer.endObject();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
  }

  public void testClosedWriterThrowsOnName() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.endArray();
    writer.close();
    try {
      writer.name("a");
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
    // Argument validation should have higher precedence
    try {
      writer.name(null);
      fail();
    } catch (NullPointerException expected) {
      assertEquals("name == null", expected.getMessage());
    }
  }

  private static void testClosedWriterThrowsOnValue(JsonTreeWriter writer) throws IOException {
    try {
      writer.value("a");
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
    try {
      writer.value(true);
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
    try {
      writer.value(Boolean.TRUE);
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
    try {
      writer.value((Boolean) null);
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
    try {
      writer.value(1.0);
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
    try {
      writer.value(1L);
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
    try {
      writer.value((Number) 1.0);
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
    try {
      writer.value((Number) null);
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }

    // Argument validation should have higher precedence
    try {
      writer.value((double) Double.NaN);
      fail();
    } catch (IllegalArgumentException expected) {
      assertEquals("JSON forbids NaN and infinities: NaN", expected.getMessage());
    }
    try {
      writer.value((Number) Double.NaN);
      fail();
    } catch (IllegalArgumentException expected) {
      assertEquals("JSON forbids NaN and infinities: NaN", expected.getMessage());
    }
  }

  public void testClosedWriterThrowsOnValue() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.endArray();
    writer.close();
    testClosedWriterThrowsOnValue(writer);
  }

  public void testClosedWriterThrowsOnPropertyValue() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setSerializeNulls(true);
    writer.beginObject();
    writer.name("test");
    try {
      writer.close();
      fail();
    } catch (IOException expected) {
      assertEquals("Incomplete document", expected.getMessage());
    }
    testClosedWriterThrowsOnValue(writer);
  }

  public void testClosedWriterThrowsOnFlush() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.endArray();
    writer.close();
    try {
      writer.flush();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Writer is closed", expected.getMessage());
    }
  }

  public void testWriterCloseIsIdempotent() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.endArray();
    writer.close();
    writer.close();
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
    try {
      writer.get();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("No value has been written yet", expected.getMessage());
    }
  }

  public void testGetImcompleteValue() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.beginObject();
    writer.name("test");
    writer.beginObject();
    writer.name("test2");
    writer.beginArray();
    writer.beginArray();
    writer.beginObject();
    writer.name("test3");
    try {
      writer.get();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("JSON value is incomplete; open values: [{{[[{:", expected.getMessage());
    }
  }

  public void testBeginArray() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    assertEquals(writer, writer.beginArray());
  }

  public void testBeginObject() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    assertEquals(writer, writer.beginObject());
  }

  public void testValueString() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    String n = "as";
    assertEquals(writer, writer.value(n));
  }

  public void testBoolValue() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    boolean bool = true;
    assertEquals(writer, writer.value(bool));
  }

  public void testBoolBoxedValue() throws Exception {
    JsonTreeWriter writer = new JsonTreeWriter();
    Boolean bool = true;
    assertEquals(writer, writer.value(bool));
  }

  public void testEmptyEndArray() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    try {
      writer.endArray();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Currently not writing an array", expected.getMessage());
    }
  }

  public void testObjectEndArray() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    try {
      writer.endArray();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Currently not writing an array", expected.getMessage());
    }
  }

  public void testEmptyEndObject() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    try {
      writer.endObject();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Currently not writing an object", expected.getMessage());
    }
  }

  public void testPendingNameEndObject() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("test");
    try {
      writer.endObject();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expecting property value before object can be closed", expected.getMessage());
    }
  }

  public void testArrayEndObject() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    try {
      writer.endObject();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Currently not writing an object", expected.getMessage());
    }
  }

  public void testEmptyStackWriteName() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    try {
      writer.name("a");
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Currently not writing an object", expected.getMessage());
    }
  }

  public void testArrayWriteName() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    try {
      writer.name("a");
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Currently not writing an object", expected.getMessage());
    }
  }

  public void testTwoNames() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("a");
    try {
      writer.name("a");
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Already wrote a name, expecting a value", expected.getMessage());
    }
  }

  public void testValueInsteadOfName() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    try {
      writer.value("a");
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Expecting a name but got a value", expected.getMessage());
    }
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

  public void testStrictMultipleTopLevelValues() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(false);
    writer.value(123);
    try {
      writer.value(123);
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("JSON must have only one top-level value.", expected.getMessage());
    }
  }

  /**
   * Even if writer is in lenient mode it should not support multiple
   * top-level values because they cannot be represented using a single
   * JsonElement.
   */
  public void testLenientMultipleTopLevelValues() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.value(123);
    try {
      writer.value(123);
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("JSON must have only one top-level value.", expected.getMessage());
    }
  }
}
