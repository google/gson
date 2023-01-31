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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import java.io.IOException;
import org.junit.Test;

@SuppressWarnings("resource")
public final class JsonElementReaderTest {

  @Test
  public void testNumbers() throws IOException {
    JsonElement element = JsonParser.parseString("[1, 2, 3]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    assertThat(reader.nextInt()).isEqualTo(1);
    assertThat(reader.nextLong()).isEqualTo(2L);
    assertThat(reader.nextDouble()).isEqualTo(3.0);
    reader.endArray();
  }

  @Test
  public void testLenientNansAndInfinities() throws IOException {
    JsonElement element = JsonParser.parseString("[NaN, -Infinity, Infinity]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.setLenient(true);
    reader.beginArray();
    assertThat(Double.isNaN(reader.nextDouble())).isTrue();
    assertThat(reader.nextDouble()).isEqualTo(Double.NEGATIVE_INFINITY);
    assertThat(reader.nextDouble()).isEqualTo(Double.POSITIVE_INFINITY);
    reader.endArray();
  }

  @Test
  public void testStrictNansAndInfinities() throws IOException {
    JsonElement element = JsonParser.parseString("[NaN, -Infinity, Infinity]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.setLenient(false);
    reader.beginArray();
    try {
      reader.nextDouble();
      fail();
    } catch (MalformedJsonException e) {
      assertThat(e.getMessage()).isEqualTo("JSON forbids NaN and infinities: NaN");
    }
    assertThat(reader.nextString()).isEqualTo("NaN");
    try {
      reader.nextDouble();
      fail();
    } catch (MalformedJsonException e) {
      assertThat(e.getMessage()).isEqualTo("JSON forbids NaN and infinities: -Infinity");
    }
    assertThat(reader.nextString()).isEqualTo("-Infinity");
    try {
      reader.nextDouble();
      fail();
    } catch (MalformedJsonException e) {
      assertThat(e.getMessage()).isEqualTo("JSON forbids NaN and infinities: Infinity");
    }
    assertThat(reader.nextString()).isEqualTo("Infinity");
    reader.endArray();
  }

  @Test
  public void testNumbersFromStrings() throws IOException {
    JsonElement element = JsonParser.parseString("[\"1\", \"2\", \"3\"]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    assertThat(reader.nextInt()).isEqualTo(1);
    assertThat(reader.nextLong()).isEqualTo(2L);
    assertThat(reader.nextDouble()).isEqualTo(3.0);
    reader.endArray();
  }

  @Test
  public void testStringsFromNumbers() throws IOException {
    JsonElement element = JsonParser.parseString("[1]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    assertThat(reader.nextString()).isEqualTo("1");
    reader.endArray();
  }

  @Test
  public void testBooleans() throws IOException {
    JsonElement element = JsonParser.parseString("[true, false]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    assertThat(reader.nextBoolean()).isEqualTo(true);
    assertThat(reader.nextBoolean()).isEqualTo(false);
    reader.endArray();
  }

  @Test
  public void testNulls() throws IOException {
    JsonElement element = JsonParser.parseString("[null,null]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    reader.nextNull();
    reader.nextNull();
    reader.endArray();
  }

  @Test
  public void testStrings() throws IOException {
    JsonElement element = JsonParser.parseString("[\"A\",\"B\"]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    assertThat(reader.nextString()).isEqualTo("A");
    assertThat(reader.nextString()).isEqualTo("B");
    reader.endArray();
  }

  @Test
  public void testArray() throws IOException {
    JsonElement element = JsonParser.parseString("[1, 2, 3]");
    JsonTreeReader reader = new JsonTreeReader(element);
    assertThat(reader.peek()).isEqualTo(JsonToken.BEGIN_ARRAY);
    reader.beginArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.NUMBER);
    assertThat(reader.nextInt()).isEqualTo(1);
    assertThat(reader.peek()).isEqualTo(JsonToken.NUMBER);
    assertThat(reader.nextInt()).isEqualTo(2);
    assertThat(reader.peek()).isEqualTo(JsonToken.NUMBER);
    assertThat(reader.nextInt()).isEqualTo(3);
    assertThat(reader.peek()).isEqualTo(JsonToken.END_ARRAY);
    reader.endArray();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testObject() throws IOException {
    JsonElement element = JsonParser.parseString("{\"A\": 1, \"B\": 2}");
    JsonTreeReader reader = new JsonTreeReader(element);
    assertThat(reader.peek()).isEqualTo(JsonToken.BEGIN_OBJECT);
    reader.beginObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.NAME);
    assertThat(reader.nextName()).isEqualTo("A");
    assertThat(reader.peek()).isEqualTo(JsonToken.NUMBER);
    assertThat(reader.nextInt()).isEqualTo(1);
    assertThat(reader.peek()).isEqualTo(JsonToken.NAME);
    assertThat(reader.nextName()).isEqualTo("B");
    assertThat(reader.peek()).isEqualTo(JsonToken.NUMBER);
    assertThat(reader.nextInt()).isEqualTo(2);
    assertThat(reader.peek()).isEqualTo(JsonToken.END_OBJECT);
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
  }

  @Test
  public void testEmptyArray() throws IOException {
    JsonElement element = JsonParser.parseString("[]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    reader.endArray();
  }

  @Test
  public void testNestedArrays() throws IOException {
    JsonElement element = JsonParser.parseString("[[],[[]]]");
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

  @Test
  public void testNestedObjects() throws IOException {
    JsonElement element = JsonParser.parseString("{\"A\":{},\"B\":{\"C\":{}}}");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("A");
    reader.beginObject();
    reader.endObject();
    assertThat(reader.nextName()).isEqualTo("B");
    reader.beginObject();
    assertThat(reader.nextName()).isEqualTo("C");
    reader.beginObject();
    reader.endObject();
    reader.endObject();
    reader.endObject();
  }

  @Test
  public void testEmptyObject() throws IOException {
    JsonElement element = JsonParser.parseString("{}");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginObject();
    reader.endObject();
  }

  @Test
  public void testSkipValue() throws IOException {
    JsonElement element = JsonParser.parseString("[\"A\",{\"B\":[[]]},\"C\",[[]],\"D\",null]");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginArray();
    assertThat(reader.nextString()).isEqualTo("A");
    reader.skipValue();
    assertThat(reader.nextString()).isEqualTo("C");
    reader.skipValue();
    assertThat(reader.nextString()).isEqualTo("D");
    reader.skipValue();
    reader.endArray();
  }

  @Test
  public void testWrongType() throws IOException {
    JsonElement element = JsonParser.parseString("[[],\"A\"]");
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
    assertThat(reader.nextString()).isEqualTo("A");
    reader.endArray();
  }

  @Test
  public void testNextJsonElement() throws IOException {
    final JsonElement element = JsonParser.parseString("{\"A\": 1, \"B\" : {}, \"C\" : []}");
    JsonTreeReader reader = new JsonTreeReader(element);
    reader.beginObject();
    try {
      reader.nextJsonElement();
      fail();
    } catch (IllegalStateException expected) {
    }
    reader.nextName();
    assertThat(new JsonPrimitive(1)).isEqualTo(reader.nextJsonElement());
    reader.nextName();
    reader.beginObject();
    try {
      reader.nextJsonElement();
      fail();
    } catch (IllegalStateException expected) {
    }
    reader.endObject();
    reader.nextName();
    reader.beginArray();
    try {
      reader.nextJsonElement();
      fail();
    } catch (IllegalStateException expected) {
    }
    reader.endArray();
    reader.endObject();
    try {
      reader.nextJsonElement();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testEarlyClose() throws IOException {
    JsonElement element = JsonParser.parseString("[1, 2, 3]");
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
