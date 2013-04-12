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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import junit.framework.TestCase;

@SuppressWarnings("resource")
public final class JsonElementReaderTest extends TestCase {

  public void testNumbers() throws IOException {
    JsonElement element = new JsonParser().parse("[1, 2, 3]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    assertEquals(1, reader.nextInt());
    assertEquals(2L, reader.nextLong());
    assertEquals(3.0, reader.nextDouble());
    reader.endArray();
  }

  public void testLenientNansAndInfinities() throws IOException {
    JsonElement element = new JsonParser().parse("[NaN, -Infinity, Infinity]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.setLenient(true);
    reader.beginArray();
    assertTrue(Double.isNaN(reader.nextDouble()));
    assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble());
    assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble());
    reader.endArray();
  }

  public void testStrictNansAndInfinities() throws IOException {
    JsonElement element = new JsonParser().parse("[NaN, -Infinity, Infinity]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.setLenient(false);
    reader.beginArray();
    try {
      reader.nextDouble();
      fail();
    } catch (NumberFormatException e) {
    }
    assertEquals("NaN", reader.nextString());
    try {
      reader.nextDouble();
      fail();
    } catch (NumberFormatException e) {
    }
    assertEquals("-Infinity", reader.nextString());
    try {
      reader.nextDouble();
      fail();
    } catch (NumberFormatException e) {
    }
    assertEquals("Infinity", reader.nextString());
    reader.endArray();
  }

  public void testNumbersFromStrings() throws IOException {
    JsonElement element = new JsonParser().parse("[\"1\", \"2\", \"3\"]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    assertEquals(1, reader.nextInt());
    assertEquals(2L, reader.nextLong());
    assertEquals(3.0, reader.nextDouble());
    reader.endArray();
  }

  public void testStringsFromNumbers() throws IOException {
    JsonElement element = new JsonParser().parse("[1]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    assertEquals("1", reader.nextString());
    reader.endArray();
  }

  public void testBooleans() throws IOException {
    JsonElement element = new JsonParser().parse("[true, false]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    assertEquals(false, reader.nextBoolean());
    reader.endArray();
  }

  public void testNulls() throws IOException {
    JsonElement element = new JsonParser().parse("[null,null]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    reader.nextNull();
    reader.nextNull();
    reader.endArray();
  }

  public void testStrings() throws IOException {
    JsonElement element = new JsonParser().parse("[\"A\",\"B\"]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    assertEquals("A", reader.nextString());
    assertEquals("B", reader.nextString());
    reader.endArray();
  }

  public void testArray() throws IOException {
    JsonElement element = new JsonParser().parse("[1, 2, 3]");
    JsonTreeReader reader = new JsonTreeReader(element);
    assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
    reader.beginArray();
    assertEquals(JsonToken.NUMBER, reader.peek());
    assertEquals(1, reader.nextInt());
    assertEquals(JsonToken.NUMBER, reader.peek());
    assertEquals(2, reader.nextInt());
    assertEquals(JsonToken.NUMBER, reader.peek());
    assertEquals(3, reader.nextInt());
    assertEquals(JsonToken.END_ARRAY, reader.peek());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testObject() throws IOException {
    JsonElement element = new JsonParser().parse("{\"A\": 1, \"B\": 2}");
    JsonTreeReader reader = new JsonTreeReader(element);
    assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
    reader.beginObject();
    assertEquals(JsonToken.NAME, reader.peek());
    assertEquals("A", reader.nextName());
    assertEquals(JsonToken.NUMBER, reader.peek());
    assertEquals(1, reader.nextInt());
    assertEquals(JsonToken.NAME, reader.peek());
    assertEquals("B", reader.nextName());
    assertEquals(JsonToken.NUMBER, reader.peek());
    assertEquals(2, reader.nextInt());
    assertEquals(JsonToken.END_OBJECT, reader.peek());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testEmptyArray() throws IOException {
    JsonElement element = new JsonParser().parse("[]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    reader.endArray();
  }

  public void testNestedArrays() throws IOException {
    JsonElement element = new JsonParser().parse("[[],[[]]]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    reader.beginArray();
    reader.endArray();
    reader.beginArray();
    reader.beginArray();
    reader.endArray();
    reader.endArray();
    reader.endArray();
  }

  public void testNestedObjects() throws IOException {
    JsonElement element = new JsonParser().parse("{\"A\":{},\"B\":{\"C\":{}}}");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginObject();
    assertEquals("A", reader.nextName());
    reader.beginObject();
    reader.endObject();
    assertEquals("B", reader.nextName());
    reader.beginObject();
    assertEquals("C", reader.nextName());
    reader.beginObject();
    reader.endObject();
    reader.endObject();
    reader.endObject();
  }

  public void testEmptyObject() throws IOException {
    JsonElement element = new JsonParser().parse("{}");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginObject();
    reader.endObject();
  }

  public void testSkipValue() throws IOException {
    JsonElement element = new JsonParser().parse("[\"A\",{\"B\":[[]]},\"C\",[[]],\"D\",null]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    assertEquals("A", reader.nextString());
    reader.skipValue();
    assertEquals("C", reader.nextString());
    reader.skipValue();
    assertEquals("D", reader.nextString());
    reader.skipValue();
    reader.endArray();
  }

  public void testWrongType() throws IOException {
    JsonElement element = new JsonParser().parse("[[],\"A\"]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    try {
      reader.nextBoolean();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.nextNull();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.nextString();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.nextInt();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.nextLong();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.nextDouble();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.nextName();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.beginObject();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.endArray();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.endObject();
      fail();
    } catch (IllegalStateException expected) {
    }
    reader.beginArray();
    reader.endArray();

    try {
      reader.nextBoolean();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.nextNull();
      fail();
    } catch (IllegalStateException expected) {
    }
    try {
      reader.nextInt();
      fail();
    } catch (NumberFormatException expected) {
    }
    try {
      reader.nextLong();
      fail();
    } catch (NumberFormatException expected) {
    }
    try {
      reader.nextDouble();
      fail();
    } catch (NumberFormatException expected) {
    }
    try {
      reader.nextName();
      fail();
    } catch (IllegalStateException expected) {
    }
    assertEquals("A", reader.nextString());
    reader.endArray();
  }

  public void testEarlyClose() throws IOException {
    JsonElement element = new JsonParser().parse("[1, 2, 3]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    reader.close();
    try {
      reader.peek();
      fail();
    } catch (IllegalStateException expected) {
    }
  }
}
