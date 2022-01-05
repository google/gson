/*
 * Copyright (C) 2014 Google Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.JsonTreeReader;
import java.io.IOException;
import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@SuppressWarnings("resource")
class JsonReaderPathTest {
  @ParameterizedTest
  @EnumSource(Factory.class)
  void path(Factory factory) throws IOException {
    JsonReader reader = factory.create("{\"a\":[2,true,false,null,\"b\",{\"c\":\"d\"},[3]]}");
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());
    reader.beginObject();
    assertEquals("$.", reader.getPreviousPath());
    assertEquals("$.", reader.getPath());
    reader.nextName();
    assertEquals("$.a", reader.getPreviousPath());
    assertEquals("$.a", reader.getPath());
    reader.beginArray();
    assertEquals("$.a[0]", reader.getPreviousPath());
    assertEquals("$.a[0]", reader.getPath());
    reader.nextInt();
    assertEquals("$.a[0]", reader.getPreviousPath());
    assertEquals("$.a[1]", reader.getPath());
    reader.nextBoolean();
    assertEquals("$.a[1]", reader.getPreviousPath());
    assertEquals("$.a[2]", reader.getPath());
    reader.nextBoolean();
    assertEquals("$.a[2]", reader.getPreviousPath());
    assertEquals("$.a[3]", reader.getPath());
    reader.nextNull();
    assertEquals("$.a[3]", reader.getPreviousPath());
    assertEquals("$.a[4]", reader.getPath());
    reader.nextString();
    assertEquals("$.a[4]", reader.getPreviousPath());
    assertEquals("$.a[5]", reader.getPath());
    reader.beginObject();
    assertEquals("$.a[5].", reader.getPreviousPath());
    assertEquals("$.a[5].", reader.getPath());
    reader.nextName();
    assertEquals("$.a[5].c", reader.getPreviousPath());
    assertEquals("$.a[5].c", reader.getPath());
    reader.nextString();
    assertEquals("$.a[5].c", reader.getPreviousPath());
    assertEquals("$.a[5].c", reader.getPath());
    reader.endObject();
    assertEquals("$.a[5]", reader.getPreviousPath());
    assertEquals("$.a[6]", reader.getPath());
    reader.beginArray();
    assertEquals("$.a[6][0]", reader.getPreviousPath());
    assertEquals("$.a[6][0]", reader.getPath());
    reader.nextInt();
    assertEquals("$.a[6][0]", reader.getPreviousPath());
    assertEquals("$.a[6][1]", reader.getPath());
    reader.endArray();
    assertEquals("$.a[6]", reader.getPreviousPath());
    assertEquals("$.a[7]", reader.getPath());
    reader.endArray();
    assertEquals("$.a", reader.getPreviousPath());
    assertEquals("$.a", reader.getPath());
    reader.endObject();
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());
  }

  @ParameterizedTest
  @EnumSource(Factory.class)
  void objectPath(Factory factory) throws IOException {
    JsonReader reader = factory.create("{\"a\":1,\"b\":2}");
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());

    reader.peek();
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());
    reader.beginObject();
    assertEquals("$.", reader.getPreviousPath());
    assertEquals("$.", reader.getPath());

    reader.peek();
    assertEquals("$.", reader.getPreviousPath());
    assertEquals("$.", reader.getPath());
    reader.nextName();
    assertEquals("$.a", reader.getPreviousPath());
    assertEquals("$.a", reader.getPath());

    reader.peek();
    assertEquals("$.a", reader.getPreviousPath());
    assertEquals("$.a", reader.getPath());
    reader.nextInt();
    assertEquals("$.a", reader.getPreviousPath());
    assertEquals("$.a", reader.getPath());

    reader.peek();
    assertEquals("$.a", reader.getPreviousPath());
    assertEquals("$.a", reader.getPath());
    reader.nextName();
    assertEquals("$.b", reader.getPreviousPath());
    assertEquals("$.b", reader.getPath());

    reader.peek();
    assertEquals("$.b", reader.getPreviousPath());
    assertEquals("$.b", reader.getPath());
    reader.nextInt();
    assertEquals("$.b", reader.getPreviousPath());
    assertEquals("$.b", reader.getPath());

    reader.peek();
    assertEquals("$.b", reader.getPreviousPath());
    assertEquals("$.b", reader.getPath());
    reader.endObject();
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());

    reader.peek();
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());
    reader.close();
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());
  }

  @ParameterizedTest
  @EnumSource(Factory.class)
  void arrayPath(Factory factory) throws IOException {
    JsonReader reader = factory.create("[1,2]");
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());

    reader.peek();
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());
    reader.beginArray();
    assertEquals("$[0]", reader.getPreviousPath());
    assertEquals("$[0]", reader.getPath());

    reader.peek();
    assertEquals("$[0]", reader.getPreviousPath());
    assertEquals("$[0]", reader.getPath());
    reader.nextInt();
    assertEquals("$[0]", reader.getPreviousPath());
    assertEquals("$[1]", reader.getPath());

    reader.peek();
    assertEquals("$[0]", reader.getPreviousPath());
    assertEquals("$[1]", reader.getPath());
    reader.nextInt();
    assertEquals("$[1]", reader.getPreviousPath());
    assertEquals("$[2]", reader.getPath());

    reader.peek();
    assertEquals("$[1]", reader.getPreviousPath());
    assertEquals("$[2]", reader.getPath());
    reader.endArray();
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());

    reader.peek();
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());
    reader.close();
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());
  }

  @Test
  void multipleTopLevelValuesInOneDocument() throws IOException {
    Factory factory = Factory.STRING_READER;
    JsonReader reader = factory.create("[][]");
    reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());
    reader.beginArray();
    reader.endArray();
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());
  }

  @ParameterizedTest
  @EnumSource(Factory.class)
  void skipArrayElements(Factory factory) throws IOException {
    JsonReader reader = factory.create("[1,2,3]");
    reader.beginArray();
    reader.skipValue();
    reader.skipValue();
    assertEquals("$[1]", reader.getPreviousPath());
    assertEquals("$[2]", reader.getPath());
  }

  @ParameterizedTest
  @EnumSource(Factory.class)
  void skipObjectNames(Factory factory) throws IOException {
    JsonReader reader = factory.create("{\"a\":1}");
    reader.beginObject();
    reader.skipValue();
    assertEquals("$.null", reader.getPreviousPath());
    assertEquals("$.null", reader.getPath());
  }

  @ParameterizedTest
  @EnumSource(Factory.class)
  void skipObjectValues(Factory factory) throws IOException {
    JsonReader reader = factory.create("{\"a\":1,\"b\":2}");
    reader.beginObject();
    assertEquals("$.", reader.getPreviousPath());
    assertEquals("$.", reader.getPath());
    reader.nextName();
    reader.skipValue();
    assertEquals("$.null", reader.getPreviousPath());
    assertEquals("$.null", reader.getPath());
    reader.nextName();
    assertEquals("$.b", reader.getPreviousPath());
    assertEquals("$.b", reader.getPath());
  }

  @ParameterizedTest
  @EnumSource(Factory.class)
  void skipNestedStructures(Factory factory) throws IOException {
    JsonReader reader = factory.create("[[1,2,3],4]");
    reader.beginArray();
    reader.skipValue();
    assertEquals("$[0]", reader.getPreviousPath());
    assertEquals("$[1]", reader.getPath());
  }

  @ParameterizedTest
  @EnumSource(Factory.class)
  void arrayOfObjects(Factory factory) throws IOException {
    JsonReader reader = factory.create("[{},{},{}]");
    reader.beginArray();
    assertEquals("$[0]", reader.getPreviousPath());
    assertEquals("$[0]", reader.getPath());
    reader.beginObject();
    assertEquals("$[0].", reader.getPreviousPath());
    assertEquals("$[0].", reader.getPath());
    reader.endObject();
    assertEquals("$[0]", reader.getPreviousPath());
    assertEquals("$[1]", reader.getPath());
    reader.beginObject();
    assertEquals("$[1].", reader.getPreviousPath());
    assertEquals("$[1].", reader.getPath());
    reader.endObject();
    assertEquals("$[1]", reader.getPreviousPath());
    assertEquals("$[2]", reader.getPath());
    reader.beginObject();
    assertEquals("$[2].", reader.getPreviousPath());
    assertEquals("$[2].", reader.getPath());
    reader.endObject();
    assertEquals("$[2]", reader.getPreviousPath());
    assertEquals("$[3]", reader.getPath());
    reader.endArray();
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());
  }

  @ParameterizedTest
  @EnumSource(Factory.class)
  void arrayOfArrays(Factory factory) throws IOException {
    JsonReader reader = factory.create("[[],[],[]]");
    reader.beginArray();
    assertEquals("$[0]", reader.getPreviousPath());
    assertEquals("$[0]", reader.getPath());
    reader.beginArray();
    assertEquals("$[0][0]", reader.getPreviousPath());
    assertEquals("$[0][0]", reader.getPath());
    reader.endArray();
    assertEquals("$[0]", reader.getPreviousPath());
    assertEquals("$[1]", reader.getPath());
    reader.beginArray();
    assertEquals("$[1][0]", reader.getPreviousPath());
    assertEquals("$[1][0]", reader.getPath());
    reader.endArray();
    assertEquals("$[1]", reader.getPreviousPath());
    assertEquals("$[2]", reader.getPath());
    reader.beginArray();
    assertEquals("$[2][0]", reader.getPreviousPath());
    assertEquals("$[2][0]", reader.getPath());
    reader.endArray();
    assertEquals("$[2]", reader.getPreviousPath());
    assertEquals("$[3]", reader.getPath());
    reader.endArray();
    assertEquals("$", reader.getPreviousPath());
    assertEquals("$", reader.getPath());
  }

  enum Factory {
    STRING_READER {
      @Override public JsonReader create(String data) {
        return new JsonReader(new StringReader(data));
      }
    },
    OBJECT_READER {
      @Override public JsonReader create(String data) {
        JsonElement element = Streams.parse(new JsonReader(new StringReader(data)));
        return new JsonTreeReader(element);
      }
    };

    abstract JsonReader create(String data);
  }
}
