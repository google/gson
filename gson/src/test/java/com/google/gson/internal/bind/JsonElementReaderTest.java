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

public final class JsonElementReaderTest extends TestCase {

  public void testArray() throws IOException {
    JsonElement array = new JsonParser().parse("[1, 2, 3]");
    JsonElementReader reader = new JsonElementReader(array);
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
    JsonElement array = new JsonParser().parse("{\"A\": 1, \"B\": 2}");
    JsonElementReader reader = new JsonElementReader(array);
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
    JsonElement array = new JsonParser().parse("[]");
    JsonElementReader reader = new JsonElementReader(array);
    reader.beginArray();
    reader.endArray();
  }

  public void testEmptyObject() throws IOException {
    JsonElement array = new JsonParser().parse("{}");
    JsonElementReader reader = new JsonElementReader(array);
    reader.beginObject();
    reader.endObject();
  }

  // TODO: more test coverage
}
